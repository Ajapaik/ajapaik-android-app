package ee.ajapaik.android.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ee.ajapaik.android.data.util.Model;

public class UploadResponse extends Model {

    private static final String KEY_PHOTOS = "photos";

    private Photo m_photo;

    public UploadResponse(JsonObject attributes) {
        JsonArray photos = attributes.getAsJsonArray(KEY_PHOTOS);
        m_photo = new Photo(photos.get(0).getAsJsonObject());
    }

    @Override
    public JsonObject getAttributes() {
        JsonObject attributes = new JsonObject();
        JsonArray photos = new JsonArray();
        photos.add(m_photo.getAttributes());
        attributes.add(KEY_PHOTOS, photos);
        return attributes;
    }

    public Photo getPhoto() {
        return m_photo;
    }

    public static final Model.Creator<UploadResponse> CREATOR = new Model.Creator<UploadResponse>() {
        @Override
        public UploadResponse newInstance(JsonObject attributes) {
            return new UploadResponse(attributes);
        }

        @Override
        public UploadResponse[] newArray(int size) {
            return new UploadResponse[size];
        }
    };
}
