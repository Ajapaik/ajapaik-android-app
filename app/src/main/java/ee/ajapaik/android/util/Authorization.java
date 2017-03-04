package ee.ajapaik.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.UUID;

public class Authorization {
    private static final String SHARED_PREFS = "prefs";
    private static final String KEY_UNIQUE_ID = "user.id";

    private static final String KEY_TYPE = "type";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_TOKEN = "token";

    public static String getUniqueIdentifier(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        if(androidId == null || androidId.length() == 0) {
            SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

            if((androidId = preferences.getString(KEY_UNIQUE_ID, "")).length() == 0) {
                SharedPreferences.Editor editor = preferences.edit();

                androidId = UUID.randomUUID().toString();
                editor.putString(KEY_UNIQUE_ID, androidId);
                editor.apply();
            }
        }

        return androidId;
    }

    public static Authorization getAnonymous(Context context) {
        String password = SHA256.encode(getUniqueIdentifier(context));

        return new Authorization(Type.ANONYMOUS, SHA1.encode(password), password);
    }

    public static Authorization parse(String str) {
        if(str != null) {
            try {
                JsonElement element = new JsonParser().parse(new JsonReader(new StringReader(str)));

                if(element.isJsonObject()) {
                    return new Authorization(element.getAsJsonObject());
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private Type m_type;
    private String m_username;
    private String m_password;
    private String m_token;

    public Authorization(JsonObject attributes) {
        JsonPrimitive primitive;

        m_type = Type.parse(((primitive = attributes.getAsJsonPrimitive(KEY_TYPE)) != null && primitive.isNumber()) ? primitive.getAsInt() : Type.UNKNOWN.getCode());
        m_username = ((primitive = attributes.getAsJsonPrimitive(KEY_USERNAME)) != null && primitive.isString()) ? primitive.getAsString() : null;
        m_password = ((primitive = attributes.getAsJsonPrimitive(KEY_PASSWORD)) != null && primitive.isString()) ? primitive.getAsString() : null;
        m_token = ((primitive = attributes.getAsJsonPrimitive(KEY_TOKEN)) != null && primitive.isString()) ? primitive.getAsString() : null;
    }

    public Authorization(Type type, String username, String password) {
        m_type = type;
        m_username = username;
        m_password = password;
    }

    public JsonObject getAttributes() {
        JsonObject attributes = new JsonObject();

        attributes.addProperty(KEY_TYPE, m_type.getCode());

        if(m_username != null) {
            attributes.addProperty(KEY_USERNAME, m_username);
        }

        if(m_password != null) {
            attributes.addProperty(KEY_PASSWORD, m_password);
        }

        if(m_token != null) {
            attributes.addProperty(KEY_TOKEN, m_token);
        }

        return attributes;
    }

    public Authorization copyWithToken(String token) {
        Authorization authorization = new Authorization(m_type, m_username, m_password);

        authorization.m_token = token;

        return authorization;
    }

    public Type getType() {
        return m_type;
    }

    public String getUsername() {
        return m_username;
    }

    public String getPassword() {
        return m_password;
    }

    public String getToken() {
        return m_token;
    }

    @Override
    public boolean equals(Object obj) {
        Authorization authorization = (Authorization)obj;

        if(authorization == this) {
            return true;
        }

        if(authorization == null ||
                authorization.m_type != m_type ||
                !Objects.match(authorization.m_username, m_username) ||
                !Objects.match(authorization.m_password, m_password) ||
                !Objects.match(authorization.m_token, m_token)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return getAttributes().toString();
    }

    public static enum Type {
        ANONYMOUS(0, "auto"),
        FACEBOOK(1, "facebook"),
        GOOGLE(2, "google"),
        USERNAME_PASSWORD(3, "ajapaik"),
        UNKNOWN(-1, null);

        private final int m_code;
        private final String m_name;

        public static Type parse(int code) {
            for(Type type : values()) {
                if(type.getCode() == code) {
                    return type;
                }
            }

            return UNKNOWN;
        }

        private Type(int code, String name) {
            m_code = code;
            m_name = name;
        }

        public int getCode() {
            return m_code;
        }

        public String getName() {
            return m_name;
        }
    }
}
