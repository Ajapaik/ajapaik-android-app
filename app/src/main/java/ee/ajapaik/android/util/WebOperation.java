package ee.ajapaik.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import ee.ajapaik.android.BuildConfig;
import ee.ajapaik.android.exception.ApiException;

public abstract class WebOperation {
    private static final String TAG = "WebOperation";

    private static final String CONTENT_ENCODING_GZIP = "gzip";

    public static final int HTTP_STATUS_OK = 200;
    public static final int HTTP_STATUS_NO_CONTENT = 204;
    public static final int HTTP_STATUS_FORBIDDEN = 403;
    public static final int HTTP_STATUS_NOT_FOUND = 404;
    public static final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500;

    private static final int RETRY_COUNT = 3;
    private static final long RETRY_INTERVAL = 500;
    private static final int TIMEOUT = 30000;

    protected Context m_context;
    private HttpClient m_client;
    private String m_url;
    private Map<String, String> m_parameters;
    private File m_file;
    private volatile boolean m_cancelled;
    protected volatile boolean m_started;
    private volatile WeakReference<ProgressListener> m_progressListener;

    public WebOperation(Context context, String url, Map<String, String> parameters) {
        this(context, url, parameters, null);
    }

    public WebOperation(Context context, String url, Map<String, String> parameters, File file) {
        m_context = context;
        m_url = url;
        m_parameters = parameters;
        m_file = file;
    }

    public void setProgressListener(ProgressListener progressListener) {
        m_progressListener = (progressListener != null) ? new WeakReference<ProgressListener>(progressListener) : null;
    }

    public String getUniqueId() {
        return null;
    }

    public boolean isCancelled() {
        return m_cancelled;
    }

    public boolean isSecure() {
        return false;
    }

    public boolean isMultipart() {
        return (m_file != null) ? true : false;
    }

    protected boolean isPost() {
        return (m_file != null) ? true : false;
    }

    protected File getFile() {
        return m_file;
    }

    protected String getUrl() {
        return m_url;
    }

    public boolean isStarted() {
        return m_started;
    }

    public boolean shouldRetry() {
        return false;
    }

    public boolean performRequest() {
        return performRequest(null, null);
    }

    public boolean performRequest(String baseURL, Map<String, String> extraParameters) {
        ConnectivityManager cm = (ConnectivityManager)m_context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        boolean isPost = isPost();
        String url = m_url;
        URI uri = null;

        if(baseURL != null && !url.contains("://")) {
            if(url.startsWith("/") && baseURL.endsWith("/")) {
                url = baseURL + url.substring(1);
            } else {
                url = baseURL + url;
            }
        }

        if(m_client == null) {
            HttpParams httpParams = new BasicHttpParams();

            HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
            HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);
            HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
            HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);
            HttpClientParams.setRedirecting(httpParams, true);

            if(url.startsWith("https")) {
                SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
                SchemeRegistry schemeRegistry = new SchemeRegistry();

                socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                schemeRegistry.register(new Scheme("https", socketFactory, 443));

                m_client = new DefaultHttpClient(new SingleClientConnManager(httpParams, schemeRegistry), httpParams);
            } else {
                m_client = new DefaultHttpClient(httpParams);
            }
        }

        m_started = true;

        if(info == null || info.getState() == NetworkInfo.State.DISCONNECTED) {
            onFailure();

            if(BuildConfig.DEBUG) {
                Log.d(TAG, "No network connection");
            }

            return false;
        }

        try {
            uri = URI.create(url);
        }
        catch(Exception e) {
            Log.w(TAG, "Unable to parse URL (" + url + ")");
            onFailure();

            return false;
        }

        for(int i = 0; i < RETRY_COUNT && !m_cancelled; i++) {
            HttpResponse response = null;
            HttpRequestBase request;

            if(isPost) {
                HttpPost postRequest = new HttpPost(uri);

                request = postRequest;

                if(m_file != null || (extraParameters != null && extraParameters.size() > 0) || (m_parameters != null && m_parameters.size() > 0)) {
                    List<NameValuePair> postData = new ArrayList<NameValuePair>(
                            ((extraParameters != null) ? extraParameters.size() : 0) + ((m_parameters != null) ? m_parameters.size() : 0));

                    if(m_parameters != null) {
                        for(Map.Entry<String, String> entry : m_parameters.entrySet()) {
                            postData.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                        }
                    }

                    if(extraParameters != null) {
                        for(Map.Entry<String, String> entry : extraParameters.entrySet()) {
                            postData.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                        }
                    }

                    try {
                        StringBuilder strData = new StringBuilder();
                        String separator = "";

                        if(m_file != null) {
                            MultipartEntity entity = new ProgressMultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                            for(NameValuePair pair : postData) {
                                strData.append(separator);
                                strData.append(pair.toString());
                                separator = "&";

                                entity.addPart(pair.getName(), new StringBody(pair.getValue()));
                            }

                            entity.addPart(getFileName(), new FileBody(m_file));
                            postRequest.setEntity(entity);
                        } else {
                            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postData, HTTP.UTF_8);

                            postRequest.setEntity(entity);

                            for(NameValuePair pair : postData) {
                                strData.append(separator);
                                strData.append(pair.toString());
                                separator = "&";
                            }
                        }

                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, strData.toString());
                        }
                    }
                    catch(UnsupportedEncodingException e) {
                        Log.d(TAG, "UTF8 is not supported");
                    }
                }
            } else if((extraParameters != null && extraParameters.size() > 0) || (m_parameters != null && m_parameters.size() > 0)) {
                Uri.Builder uriBuilder = Uri.parse(m_url).buildUpon();

                if(m_parameters != null) {
                    for(Map.Entry<String, String> entry : m_parameters.entrySet()) {
                        uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
                    }
                }

                if(extraParameters != null) {
                    for(Map.Entry<String, String> entry : extraParameters.entrySet()) {
                        uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
                    }
                }

                request = new HttpGet(URI.create(uriBuilder.build().toString()));
            } else {
                request = new HttpGet(uri);
            }

            request.setHeader("Accept-Encoding", CONTENT_ENCODING_GZIP);

            try {
                Header encoding = null;
                HttpEntity entity;

                if(BuildConfig.DEBUG) {
                    Log.e(TAG, ((isPost) ? "POST: " : "GET: ") + request.getURI().toString());
                }

                response = m_client.execute(request);

                if((entity = response.getEntity()) != null) {
                    encoding = entity.getContentEncoding();
                }

                try {
                    onResponse(
                            response.getStatusLine().getStatusCode(),
                            (encoding != null && CONTENT_ENCODING_GZIP.equals(encoding.getValue())) ?
                                    new GZIPInputStream(entity.getContent()) : entity.getContent());
                } catch (ApiException e) {
                    Crashlytics.log(e.toString());
                    Crashlytics.setString("URL", url);
                    if ( m_parameters != null && !m_parameters.isEmpty()) {
                        Crashlytics.setString("params", new JSONObject(m_parameters).toString());
                    }
                    Crashlytics.logException(e);
                }

                return true;
            }
            catch(IOException e) {
                if(BuildConfig.DEBUG) {
                    Log.w(TAG, "Network error", e);
                }

                try {
                    HttpEntity entity = response.getEntity();

                    entity.consumeContent();
                    entity.getContent().close();
                }
                catch(Exception e1) {
                }

                request.abort();

                try {
                    Thread.sleep(RETRY_INTERVAL);
                } catch (InterruptedException e1) {
                }
            }
        }

        return true;
    }

    public void abortRequest() {
        m_cancelled = true;
    }

    protected String getFileName() {
        return "original";
    }

    protected void onFailure() { }
    protected abstract void onResponse(int statusCode, InputStream stream) throws ApiException;

    public interface ProgressListener {
        public void onProgress(long progress);
    }

    public interface ResultHandler {
        public void onResult(int error, Parcelable data);
    }

    private class ProgressMultipartEntity extends MultipartEntity {
        public ProgressMultipartEntity(HttpMultipartMode mode) {
            super(mode);
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            super.writeTo(new ProgressOutputStream(out));
        }

        private class ProgressOutputStream extends FilterOutputStream {
            private long m_progress = 0;

            public ProgressOutputStream(OutputStream out) {
                super(out);
            }

            @Override
            public void write(byte[] buffer, int offset, int length) throws IOException {
                out.write(buffer, offset, length);
                m_progress += length;

                if(m_progressListener != null) {
                    ProgressListener listener = m_progressListener.get();

                    if(listener != null) {
                        listener.onProgress(m_progress);
                    }
                }
            }

            @Override
            public void write(int oneByte) throws IOException {
                out.write(oneByte);
                m_progress += 1;

                if(m_progressListener != null) {
                    ProgressListener listener = m_progressListener.get();

                    if(listener != null) {
                        listener.onProgress(m_progress);
                    }
                }
            }
        }
    }
}
