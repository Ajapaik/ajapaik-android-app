package ee.ajapaik.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private CloseableHttpClient m_client;
    private Settings m_settings;
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
        m_settings = new Settings(context);
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
        return performRequest(null, null, null);
    }

    public boolean performRequest(String baseURL, Map<String, String> extraParameters, BasicCookieStore cookieStore) {
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

        if (m_client == null) {
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "new m_client");
            }

            try {
                m_client = HttpClients.custom()
                        .setDefaultCookieStore(cookieStore)
                        .build();

            }
            catch(Exception e) {
                Log.w(TAG, e.toString());
            }
        }
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "m_client ready");
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
            CloseableHttpResponse response = null;
            HttpUriRequestBase request;

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
                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                        if(m_file != null) {
                            Log.d(TAG, "Adding file to post");
                            for(NameValuePair pair : postData) {
                                StringBody stringBody1 = new StringBody(pair.getValue().toString(), ContentType.MULTIPART_FORM_DATA);
                                builder.addPart(pair.getName(), stringBody1);

                                strData.append(separator);
                                strData.append(pair.toString());
                                separator = "&";
                            }
                            FileBody fileBody = new FileBody(m_file, ContentType.DEFAULT_BINARY);
                            builder.addPart("original", fileBody);
                            HttpEntity entity = builder.build();
                            postRequest.setEntity(entity);
                        } else {
                            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postData);

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
                    catch(Exception e) {
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
                String encoding = null;
                HttpEntity entity;

                if(BuildConfig.DEBUG) {
                    Log.e(TAG, "Retry count:" + i);
                    Log.e(TAG, ((isPost) ? "POST: " : "GET: ") + request.toString());
                    Log.e(TAG, "Cookies before: " + cookieStore.getCookies().toString());
                }

                response = m_client.execute(request);


                if((entity = response.getEntity()) != null) {
                    encoding = entity.getContentEncoding();
                }
                try {
                    onResponse(
                            response.getCode(),
                            (encoding != null && CONTENT_ENCODING_GZIP.equals(encoding)) ?
                                    new GZIPInputStream(entity.getContent()) : entity.getContent());
                    Log.e(TAG, "responseCode: " + response.getCode());
                    Log.e(TAG, "Cookies after: " + cookieStore.getCookies().toString());
                    List<Cookie> cookies = cookieStore.getCookies();
                    String session_id=null;

                    for (int n = 0; n < cookies.size(); n++) {

                        if (cookies.get(n).getName().equals("sessionid")) {
                            Log.d(TAG, "Cookie :" + cookies.get(n).getName() + "; value " + cookies.get(n).getValue());
                            session_id=cookies.get(n).getValue();
                        }
                    }

                    // If no session_id in response then kill login
                    if (session_id==null) {
                        Log.e(TAG, "No session_id in response cookie");
                        m_settings.setSession(null);
                    }

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
}
