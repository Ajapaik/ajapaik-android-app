package ee.ajapaik.android.data;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import ee.ajapaik.android.data.util.Model;
import ee.ajapaik.android.util.Dates;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

public class Upload extends Model {
    private static final String TAG = "Upload";

    private static final String API_PATH = "/photo/upload/";

    private static final String FOLDER_NAME = "Ajapaik-rephotos";

    private static final String KEY_PATH = "path";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_ACCURACY = "accuracy";
    private static final String KEY_AGE = "age";
    private static final String KEY_DATE = "date";
    private static final String KEY_FLIP = "flip";
    private static final String KEY_YAW = "yaw";
    private static final String KEY_PITCH = "pitch";
    private static final String KEY_ROLL = "roll";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_SCALE = "scale";

    public static WebAction<Upload> createAction(Context context, Upload upload) {
        Map<String, String> parameters = new Hashtable<String, String>();

        parameters.put("id", upload.getPhoto().getIdentifier());
        parameters.put(KEY_SCALE, Float.toString(upload.getScale()));
        parameters.put(KEY_FLIP, Integer.toString((upload.m_flip) ? 1 : 0));

        if(upload.m_date != null) {
            parameters.put(KEY_DATE, Dates.toDDMMYYYYString(upload.m_date));
        }

        if(upload.m_age > 0) {
            parameters.put(KEY_AGE, Long.toString(upload.m_age));
            parameters.put(KEY_LATITUDE, Double.toString(upload.m_latitude));
            parameters.put(KEY_LONGITUDE, Double.toString(upload.m_longitude));
            parameters.put(KEY_ACCURACY, Float.toString(upload.m_accuracy));
        }

        parameters.put(KEY_YAW, Float.toString(upload.m_yaw));
        parameters.put(KEY_PITCH, Float.toString(upload.m_pitch));
        parameters.put(KEY_ROLL, Float.toString(upload.m_roll));

        return new WebAction<Upload>(context, API_PATH, parameters, new File(upload.getPath()), null);
    }

    private String m_path;
    private Date m_date;
    private Photo m_photo;
    private double m_latitude;
    private double m_longitude;
    private long m_age;
    private float m_accuracy;
    private float m_scale;
    private boolean m_flip;
    private float m_yaw;
    private float m_pitch;
    private float m_roll;

    public static File getFolder() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + FOLDER_NAME);
    }

    public static Upload parse(String str) {
        if(str != null) {
            try {
                JsonElement element = new JsonParser().parse(new JsonReader(new StringReader(str)));

                if(element.isJsonObject()) {
                    return new Upload(element.getAsJsonObject());
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public Upload(JsonObject attributes) {
        JsonPrimitive primitive;
        JsonElement photo;

        m_path = ((primitive = attributes.getAsJsonPrimitive(KEY_PATH)) != null && primitive.isString()) ? primitive.getAsString() : null;
        m_date = ((primitive = attributes.getAsJsonPrimitive(KEY_DATE)) != null && primitive.isString()) ? Dates.parse(primitive.getAsString()) : null;
        m_photo = ((photo = attributes.get(KEY_PHOTO)) != null && photo.isJsonObject()) ? new Photo(photo.getAsJsonObject()) : null;
        m_scale = ((primitive = attributes.getAsJsonPrimitive(KEY_SCALE)) != null && primitive.isNumber()) ? primitive.getAsFloat() : 1.0F;
        m_latitude = ((primitive = attributes.getAsJsonPrimitive(KEY_LATITUDE)) != null && primitive.isNumber()) ? primitive.getAsDouble() : 0.0F;
        m_longitude = ((primitive = attributes.getAsJsonPrimitive(KEY_LONGITUDE)) != null && primitive.isNumber()) ? primitive.getAsDouble() : 0.0F;
        m_accuracy = ((primitive = attributes.getAsJsonPrimitive(KEY_ACCURACY)) != null && primitive.isNumber()) ? primitive.getAsFloat() : 0.0F;
        m_age = ((primitive = attributes.getAsJsonPrimitive(KEY_AGE)) != null && primitive.isNumber()) ? primitive.getAsInt() : 0;
        m_flip = ((primitive = attributes.getAsJsonPrimitive(KEY_FLIP)) != null && primitive.isBoolean()) ? primitive.getAsBoolean() : false;
        m_yaw = ((primitive = attributes.getAsJsonPrimitive(KEY_YAW)) != null && primitive.isNumber()) ? primitive.getAsFloat() : 0.0F;
        m_pitch = ((primitive = attributes.getAsJsonPrimitive(KEY_PITCH)) != null && primitive.isNumber()) ? primitive.getAsFloat() : 0.0F;
        m_roll = ((primitive = attributes.getAsJsonPrimitive(KEY_ROLL)) != null && primitive.isNumber()) ? primitive.getAsFloat() : 0.0F;
    }

    public Upload(Photo photo, boolean flip, float scale, String path, Location location, float[] orientation) {
        if(path == null) {
            File folder = Upload.getFolder();

            folder.mkdirs();
            path = folder.getAbsolutePath() + File.separator + "Ajapaik-rephoto-" + Dates.toFilename(new Date()) + ".jpg";
        }

        m_date = new Date();
        m_path = path;
        m_photo = photo;
        m_flip = flip;
        m_scale = scale;

        if(location != null) {
            m_latitude = location.getLatitude();
            m_longitude = location.getLongitude();
            m_accuracy = location.getAccuracy();
            m_age = Math.abs((m_date.getTime() - location.getTime()) / 1000);
        }

        if(orientation != null && orientation.length >= 3) {
            m_yaw = orientation[0];
            m_pitch = orientation[1];
            m_roll = orientation[2];
        }
    }

    public boolean isFlipped() {
        return m_flip;
    }

    public String getPath() {
        return m_path;
    }

    public Date getDate() {
        return m_date;
    }

    public Photo getPhoto() {
        return m_photo;
    }

    public float getScale() {
        return m_scale;
    }

    public Uri getLocalUri() {
        return Uri.fromFile(new File(m_path));
    }

    public boolean save(byte[] data) {
        FileOutputStream stream = null;
        File file = new File(m_path);

        if(file.exists()) {
            Log.w(TAG, "Going to overwrite picture at " + file.getName());
        }

        try {
            stream = new FileOutputStream(m_path);
            stream.write(data);
            Log.d(TAG, "Image written to " + m_path);
            return true;
        }
        catch(Exception e) {
            Log.w(TAG, "Saving file failed", e);
        }
        finally {
            if(stream != null) {
                try {
                    stream.close();
                }
                catch(IOException e) {
                    Log.d(TAG, "", e);
                }
            }
        }
        return false;
    }

    public boolean unsave() {
        File file = new File(m_path);

        return file.delete();
    }

    public JsonObject getAttributes() {
        JsonObject attributes = new JsonObject();

        attributes.addProperty(KEY_SCALE, m_scale);

        if(m_path != null) {
            attributes.addProperty(KEY_PATH, m_path);
        }

        if(m_photo != null) {
            attributes.add(KEY_PHOTO, m_photo.getAttributes());
        }

        if(m_date != null) {
            attributes.addProperty(KEY_DATE, Dates.toString(m_date));
        }

        attributes.addProperty(KEY_FLIP, m_flip);

        if(m_age > 0) {
            attributes.addProperty(KEY_AGE, m_age);
            attributes.addProperty(KEY_LATITUDE, m_latitude);
            attributes.addProperty(KEY_LONGITUDE, m_longitude);
            attributes.addProperty(KEY_ACCURACY, m_accuracy);
        }

        attributes.addProperty(KEY_YAW, m_yaw);
        attributes.addProperty(KEY_PITCH, m_pitch);
        attributes.addProperty(KEY_ROLL, m_roll);

        return attributes;
    }

    @Override
    public String toString() {
        return getAttributes().toString();
    }

    @Override
    public boolean equals(Object obj) {
        Upload upload = (Upload)obj;

        if(upload == this) {
            return true;
        }

        if(upload == null ||
                !Objects.match(upload.getPhoto(), m_photo) ||
                !Objects.match(upload.getPath(), m_path) ||
                !Objects.match(upload.getDate(), m_date) ||
                upload.m_flip != m_flip ||
                upload.m_scale != m_scale ||
                upload.m_age != m_age ||
                upload.m_latitude != m_latitude ||
                upload.m_longitude != m_longitude ||
                upload.m_accuracy != m_accuracy ||
                upload.m_yaw != m_yaw ||
                upload.m_pitch != m_pitch ||
                upload.m_roll != m_roll) {
            return false;
        }

        return true;
    }

    public static final Model.Creator<Upload> CREATOR = new Model.Creator<Upload>() {
        @Override
        public Upload newInstance(JsonObject attributes) {
            return new Upload(attributes);
        }

        @Override
        public Upload[] newArray(int size) {
            return new Upload[size];
        }
    };
}
