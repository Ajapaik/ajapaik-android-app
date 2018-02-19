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

import ee.ajapaik.android.data.Upload;

import static ee.ajapaik.android.util.ExifService.USER_COMMENT;
import static org.apache.http.util.TextUtils.isBlank;

public class RephotoDraftService {

    public List<Upload> getAllDraftsFor(String identifier) {
        File[] images = Upload.getFolder().listFiles();

        List<Upload> result = new ArrayList<>();

        for (File file : images) {
            Upload upload = getUpload(file);
            if (upload == null) continue;
            if (identifier.equals(upload.getPhoto().getIdentifier())) {
                result.add(upload);
            }
        }
        return result;
    }

    public Map<String, List<Upload>> getAllDrafts(String searchQuery) {
        File[] images = Upload.getFolder().listFiles();

        final Map<String, List<Upload>> uploadsByPhoto = new HashMap<>();

        for (File file : images) {
            Upload upload = getUpload(file);
            if (upload == null) continue;
            if (searchQuery != null && !matchesSearchQuery(upload, searchQuery)) continue;
            String identifier = upload.getPhoto().getIdentifier();
            List<Upload> uploads = uploadsByPhoto.get(identifier);
            if (uploads == null) uploads = new ArrayList<>();
            uploads.add(upload);
            uploadsByPhoto.put(identifier, uploads);
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
