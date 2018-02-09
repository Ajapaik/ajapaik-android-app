package ee.ajapaik.android.data;

import android.net.Uri;

import com.google.gson.JsonObject;

import java.util.Date;

import ee.ajapaik.android.data.util.Model;
import ee.ajapaik.android.data.util.PhotoModel;

public class Rephoto extends PhotoModel {

    private Date m_date;
    private String m_author;
    private boolean m_isUploadedByCurrentUser;

    public Rephoto(JsonObject jsonObject) {
        this.m_image = readUri(jsonObject, "image");
        this.m_date = readDate(jsonObject, "date");
        this.m_author = readString(jsonObject, "user_name");
        this.m_isUploadedByCurrentUser = readBoolean(jsonObject, "is_uploaded_by_current_user");
    }

    @Override
    public JsonObject getAttributes() {
        JsonObject attributes = new JsonObject();
        write(attributes, "image", m_image);
        writeDDMMYYYYDate(attributes, "date", m_date);
        write(attributes, "user_name", m_author);
        write(attributes, "is_uploaded_by_current_user", m_isUploadedByCurrentUser);
        return attributes;
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
}
