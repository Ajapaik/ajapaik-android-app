package ee.ajapaik.android.data;

import com.google.gson.JsonObject;

import java.util.Date;

import ee.ajapaik.android.data.util.Model;

public class Rephoto extends Model {

    private String image;
    private Date date;
    private String author;
    private boolean isUploadedByCurrentUser;

    public Rephoto(JsonObject jsonObject) {
        this.image = readString(jsonObject, "image");
        this.date = readDate(jsonObject, "date");
        this.author = readString(jsonObject, "user_name");
        this.isUploadedByCurrentUser = readBoolean(jsonObject, "is_uploaded_by_current_user");
    }

    @Override
    public JsonObject getAttributes() {
        JsonObject attributes = new JsonObject();
        write(attributes, "image", image);
        writeDDMMYYYYDate(attributes, "date", date);
        write(attributes, "user_name", author);
        write(attributes, "is_uploaded_by_current_user", isUploadedByCurrentUser);
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

    public String getImage() {
        return image;
    }

    public Date getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isUploadedByCurrentUser() {
        return isUploadedByCurrentUser;
    }
}
