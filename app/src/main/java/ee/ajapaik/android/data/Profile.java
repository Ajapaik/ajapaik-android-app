package ee.ajapaik.android.data;

import android.content.Context;

import com.google.gson.JsonObject;

import java.util.Hashtable;
import java.util.Map;

import ee.ajapaik.android.data.util.Model;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebAction;

public class Profile extends Model {
    private static final String API_PATH = "/user/me/";
    private static final String KEY_NAME = "name";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_LINK = "link";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_STATE = "state";
    private static final String KEY_REPHOTOS = "rephotos";
    private static final String KEY_RANK = "rank";

    public static WebAction<Profile> createAction(Context context, Profile profile) {
        Map<String, String> parameters = new Hashtable<String, String>();

        if(profile != null && profile.getState() != null) {
            parameters.put("state", profile.getState());
        }

        return new Action(context, API_PATH, parameters, profile);
    }

    public static Profile parse(String str) {
        return CREATOR.parse(str);
    }

    private String m_name;
    private Hyperlink m_avatar;
    private String m_message;
    private Hyperlink m_link;
    private int m_rank;
    private String m_state;
    private int m_rephotos;

    public Profile() {
        m_rank = -1;
        m_rephotos = -1;
    }

    public Profile(JsonObject attributes) {
        this(attributes, null);
    }

    public Profile(JsonObject attributes, Profile baseProfile) {
        m_link = readHyperlink(attributes, KEY_LINK);
        m_name = readString(attributes, KEY_NAME);
        m_avatar = readHyperlink(attributes, KEY_AVATAR);
        m_message = readString(attributes, KEY_MESSAGE);
        m_rank = readInteger(attributes, KEY_RANK);
        m_rephotos = readInteger(attributes, KEY_REPHOTOS);
        m_state = readString(attributes, KEY_STATE);
    }

    public boolean isObsolete() {
        return (m_rank < 0 || m_rephotos < 0) ? true : false;
    }

    @Override
    public JsonObject getAttributes() {
        JsonObject attributes = new JsonObject();

        write(attributes, KEY_LINK, m_link);
        write(attributes, KEY_NAME, m_name);
        write(attributes, KEY_AVATAR, m_avatar);
        write(attributes, KEY_MESSAGE, m_message);
        write(attributes, KEY_STATE, m_state);
        write(attributes, KEY_RANK, m_rank);
        write(attributes, KEY_REPHOTOS, m_rephotos);

        return attributes;
    }

    public Hyperlink getLink() {
        return m_link;
    }

    public String getName() {
        return m_name;
    }

    public Hyperlink getAvatar() {
        return m_avatar;
    }

    public String getMessage() {
        return m_message;
    }

    public String getState() {
        return m_state;
    }

    public int getRank() {
        return m_rank;
    }

    public int getRephotosCount() {
        return m_rephotos;
    }

    @Override
    public boolean equals(Object obj) {
        Profile profile = (Profile)obj;

        if(profile == this) {
            return true;
        }

        if(profile == null ||
                profile.getRephotosCount() != m_rephotos ||
                profile.getRank() != m_rank ||
                !Objects.match(profile.getLink(), m_link) ||
                !Objects.match(profile.getMessage(), m_message) ||
                !Objects.match(profile.getAvatar(), m_avatar) ||
                !Objects.match(profile.getName(), m_name) ||
                !Objects.match(profile.getState(), m_state)) {
            return false;
        }

        return true;
    }

    private static class Action extends WebAction<Profile> {
        private Profile m_baseProfile;

        public Action(Context context, String path, Map<String, String> parameters, Profile baseProfile) {
            super(context, path, parameters, CREATOR);
            m_baseProfile = baseProfile;
        }

        @Override
        public String getUniqueId() {
            return (m_baseProfile != null && m_baseProfile.getState() != null) ? getUrl() + "/" + m_baseProfile.getState() : null;
        }

        @Override
        protected Profile parseObject(JsonObject attributes) {
            return new Profile(attributes, m_baseProfile);
        }
    }

    public static final Model.Creator<Profile> CREATOR = new Model.Creator<Profile>() {
        @Override
        public Profile newInstance(JsonObject attributes) {
            return new Profile(attributes);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}

