package ee.ajapaik.android.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.Upload;

import static ee.ajapaik.android.util.ExifService.USER_COMMENT;
import static org.apache.http.util.TextUtils.isBlank;

public class RephotoDraftService {

    public Map<Photo, List<Upload>> getAllDrafts(String searchQuery) {
        File[] images = Upload.getFolder().listFiles();

        final Map<Photo, List<Upload>> uploadsByPhoto = new HashMap<>();

        for (File file : images) {
            Upload upload = getUpload(file);
            if (upload == null) continue;
            if (searchQuery != null && !matchesSearchQuery(upload, searchQuery)) continue;
            Photo photo = upload.getPhoto();
            List<Upload> uploads = uploadsByPhoto.get(photo);
            if (uploads == null) uploads = new ArrayList<>();
            uploads.add(upload);
            uploadsByPhoto.put(photo, uploads);
        }

        return uploadsByPhoto;
    }

    private boolean matchesSearchQuery(Upload upload, String searchQuery) {
        String title = upload.getPhoto().getTitle();
        return title != null && title.toLowerCase().contains(searchQuery.toLowerCase());
    }

    private Upload getUpload(File file) {
        String uploadJsonString = getUploadData(file);
        if (isBlank(uploadJsonString)) return null;

        JsonObject uploadJson = new JsonParser().parse(new JsonReader(new StringReader(uploadJsonString))).getAsJsonObject();
        return new Upload(uploadJson);
    }

    private String getUploadData(File file) {
        return ExifService.readField(file, USER_COMMENT);
    }
}
