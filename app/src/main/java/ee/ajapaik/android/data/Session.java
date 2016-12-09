package ee.ajapaik.android.data;

import android.content.Context;

import com.google.gson.JsonObject;

import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import ee.ajapaik.android.data.util.Model;
import ee.ajapaik.android.util.Authorization;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebAction;

public class Session extends Model {
    private static final String KEY_EXPIRES = "expires.abs";
    private static final String KEY_SESSION_LENGTH = "expires";
    private static final String KEY_SESSION = "session";
    private static final String KEY_USER = "id";

    private static final String KEY_WEB_LANGUAGE = "_l";
    private static final String KEY_WEB_TOKEN = "_s";
    private static final String KEY_WEB_USER = "_u";

    private static final int DEFAULT_SESSION_LENGTH_IN_SECONDS = 60;

    public static WebAction<Session> createRegisterAction(Context context, Authorization authorization) {
        Map<String, String> parameters = new Hashtable<String, String>();

        parameters.put("os", "android");
        parameters.put("version", "0.2");
        parameters.put("type", authorization.getType().getName());
        parameters.put("username", authorization.getUsername());
        parameters.put("password", authorization.getPassword());

        return new WebAction<Session>(context, "/register/", parameters, CREATOR);
    }

    public static WebAction<Session> createLoginAction(Context context, Authorization authorization) {
        Map<String, String> parameters = new Hashtable<String, String>();

        parameters.put("os", "android");
        parameters.put("version", "0.2");
        parameters.put("type", authorization.getType().getName());
        parameters.put("username", authorization.getUsername());
        parameters.put("password", authorization.getPassword());

        return new Action(context, "/login/", parameters);
    }

    public static WebAction<Session> createLogoutAction(Context context) {
        return new WebAction<Session>(context, "/logout/", null, CREATOR);
    }

    public static Session parse(String str) {
        return CREATOR.parse(str);
    }

    private long m_expires;
    private String m_token;
    private String m_user;

    public Session(JsonObject attributes) {
        long expires = readLong(attributes, KEY_EXPIRES);

        m_token = readString(attributes, KEY_SESSION);
        m_user = readIdentifier(attributes, KEY_USER);

        if(expires == 0) {
            expires = readInteger(attributes, KEY_SESSION_LENGTH);
            m_expires = new Date().getTime() + ((expires > 0) ? expires : DEFAULT_SESSION_LENGTH_IN_SECONDS) * 1000;
        } else {
            m_expires = expires;
        }

        if(m_user == null || m_token == null) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public JsonObject getAttributes() {
        JsonObject attributes = new JsonObject();

        if(m_token != null) {
            attributes.addProperty(KEY_SESSION, m_token);
        }

        if(m_user != null) {
            attributes.addProperty(KEY_USER, m_user);
        }

        attributes.addProperty(KEY_EXPIRES, m_expires);

        return attributes;
    }

    public Map<String, String> getWebParameters() {
        return getWebParameters(null);
    }

    public Map<String, String> getWebParameters(String language) {
        Map<String, String> parameters = new Hashtable<String, String>();

        parameters.put(KEY_WEB_TOKEN, m_token);
        parameters.put(KEY_WEB_USER, m_user);

        if(language == null || language.length() == 0) {
            Locale locale = Locale.getDefault();

            if(locale != null) {
                language = locale.getLanguage();
            }
        }

        if(language != null) {
            parameters.put(KEY_WEB_LANGUAGE, language);
        }

        return parameters;
    }

    public long getExpires() {
        return m_expires;
    }

    public String getToken() {
        return m_token;
    }

    public String getUser() {
        return m_user;
    }

    public boolean isExpired() {
        return (new Date().getTime() > m_expires) ? true : false;
    }

    @Override
    public boolean equals(Object obj) {
        Session session = (Session)obj;

        if(session == this) {
            return true;
        }

        if(session == null ||
           session.getExpires() != m_expires ||
           !Objects.match(session.getToken(), m_token) ||
           !Objects.match(session.getUser(), m_user)) {
            return false;
        }

        return true;
    }

    private static class Action extends WebAction<Session> {
        public Action(Context context, String path, Map<String, String> parameters) {
            super(context, path, parameters, CREATOR);
        }

        @Override
        public boolean isSecure() {
            return false;
        }
    }

    public static final Model.Creator<Session> CREATOR = new Model.Creator<Session>() {
        @Override
        public Session newInstance(JsonObject attributes) {
            return new Session(attributes);
        }

        @Override
        public Session[] newArray(int size) {
            return new Session[size];
        }
    };
}
