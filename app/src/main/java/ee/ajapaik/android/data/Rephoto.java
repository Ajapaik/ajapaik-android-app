package ee.ajapaik.android.data;

import android.content.Context;
import android.net.Uri;

import com.google.gson.JsonObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ee.ajapaik.android.data.util.Model;
import ee.ajapaik.android.data.util.PhotoModel;
import ee.ajapaik.android.util.WebAction;

public class Rephoto extends PhotoModel {

    private static final String API_RATE_REPHOTO_PATH = "/rephoto/rate/";

    public static WebAction<Rephoto> createRatingChangeAction(Context context, float rating,
                                                              String identifier) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("id", identifier);
        parameters.put("rating", String.valueOf(rating));

        return new Action(context, API_RATE_REPHOTO_PATH, parameters, "rating-" + rating);
    }

    private String m_id;
    private Date m_date;
    private String m_author;
    private boolean m_isUploadedByCurrentUser;

    public Rephoto(JsonObject jsonObject) {
        this.m_id = readString(jsonObject, "id");
        this.m_image = readUri(jsonObject, "image");
        this.m_date = readDate(jsonObject, "date");
        this.m_author = readString(jsonObject, "user_name");
        this.m_isUploadedByCurrentUser = readBoolean(jsonObject, "is_uploaded_by_current_user");
    }

    @Override
    public JsonObject getAttributes() {
        JsonObject attributes = new JsonObject();
        write(attributes, "id", m_id);
        write(attributes, "image", m_image);
        writeDDMMYYYYDate(attributes, "date", m_date);
        write(attributes, "user_name", m_author);
        write(attributes, "is_uploaded_by_current_user", m_isUploadedByCurrentUser);
        return attributes;
    }

    public String getId() {
        return m_id;
    }

    public Uri getImage() {
        return m_image;
    }

    public Date getDate() {
        return m_date;
    }

    public String getAuthor() {
        return m_author;
    }

    public boolean isUploadedByCurrentUser() {
        return m_isUploadedByCurrentUser;
    }

    private static class Action extends WebAction<Rephoto> {

        private String m_baseIdentifier;

        public Action(Context context, String path, Map<String, String> parameters, String baseIdentifier) {
            super(context, path, parameters, CREATOR);
            m_baseIdentifier = baseIdentifier;
        }

        @Override
        public String getUniqueId() {
            return getUrl() + m_baseIdentifier;
        }

        @Override
        protected Rephoto parseObject(JsonObject attributes) {
            return new Rephoto(attributes);
        }
    }

    public static final Model.Creator<Rephoto> CREATOR = new Model.Creator<Rephoto>() {
        @Override
        public Rephoto newInstance(JsonObject attributes) {
            return new Rephoto(attributes);
        }

        @Override
        public Rephoto[] newArray(int size) {
            return new Rephoto[size];
        }
    };
}
