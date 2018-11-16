package ee.ajapaik.android.util;

import android.arch.core.BuildConfig;
import android.content.Context;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import ee.ajapaik.android.data.util.Model;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.exception.ApiException;

public class WebAction<T> extends WebOperation {
    private static final String TAG = "WebAction";

    private static final String KEY_ERROR = "error";

    private Model.Creator<T> m_creator;
    private Status m_status = Status.UNKNOWN;
    private T m_object;

    public WebAction(Context context, String path, Map<String, String> parameters, Model.Creator<T> creator) {
        this(context, path, parameters, null, creator);
    }

    public WebAction(Context context, String path, Map<String, String> parameters, File file, Model.Creator<T> creator) {
        super(context, path, parameters, file);

        m_creator = creator;
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public boolean isPost() {
        return true;
    }

    public Status getStatus() {
        return m_status;
    }

    public T getObject() {
        return m_object;
    }

    protected T parseObject(JsonObject attributes) {
        return (m_creator != null) ? m_creator.newInstance(attributes) : null;
    }

    @Override
    public boolean shouldRetry() {
        return !isCancelled() && isSecure() && m_status.isSessionProblem();
    }

    @Override
    protected void onFailure() {
        m_status = Status.CONNECTION;
    }

    @Override
    protected void onResponse(int statusCode, InputStream stream) throws ApiException {
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "statusCode=" + statusCode + ", stream=" + ((stream != null) ? "YES" : "NONE"));
        }

        boolean isParsableObject = false;

        if((statusCode == HTTP_STATUS_OK || statusCode == HTTP_STATUS_FORBIDDEN || statusCode == HTTP_STATUS_INTERNAL_SERVER_ERROR) && stream != null) {
            try {
                JsonElement element = new JsonParser().parse(new JsonReader(new InputStreamReader(stream, "UTF-8")));

                if(element.isJsonObject()) {
                    JsonObject attributes = element.getAsJsonObject();
                    JsonPrimitive error = attributes.getAsJsonPrimitive(KEY_ERROR);

                    if(error != null) {
                        if (error.isNumber()) {
                            m_status = Status.parse(error.getAsInt());
                        } else if (error.isString()) {
                            m_status = Status.parse(error.getAsString());
                        } else {
                            m_status = Status.UNKNOWN;
                        }
                    } else {
                        m_status = Status.NONE;
                    }

                    if (m_status != Status.NONE) {
                        throw new ApiException(element);
                    }

                    isParsableObject = isParsableObject(attributes);
                    if (isParsableObject) {
                        m_object = parseObject(attributes);
                    }
                }
            }
            catch (UnsupportedEncodingException e) {
                Log.w(TAG, "Parse error", e);
            }
        } else {
            m_status = Status.CONNECTION;

            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.w(TAG, "", e);
                }
            }
        }

        if(m_status == Status.NONE && m_object == null && m_creator != null && isParsableObject) {
            m_status = Status.INVALID_DATA;
        }
    }

    private boolean isParsableObject(JsonObject attributes) {
        return attributes.size() > (attributes.has(KEY_ERROR) ? 1 : 0);
    }

    public interface ResultHandler<T> {
        void onActionResult(Status status, T data);
    }
}
