package ee.ajapaik.android.fragment;


import android.os.Bundle;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import ee.ajapaik.android.R;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.Upload;

import static org.apache.http.util.TextUtils.isBlank;

public class LocalRephotosFragment extends PhotosFragment {

    private static final String TAG = "LocalRephotosFragment";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refresh();
    }

    @Override
    protected void refresh() {
        String[] UploadFiles = getContext().getFilesDir().list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith(Upload.INTERNAL_STORAGE_FILE_SUFFIX);
            }
        });

        List<Photo> photos = new ArrayList<>();
        for (String fileName : UploadFiles) {
            Upload upload = getUpload(fileName);
            if (upload == null) continue;
            photos.add(upload.getPhoto());
        }

        if (photos.isEmpty()) {
            initializeEmptyGridView(getGridView());
        } else {
            Album album = new Album(photos, "local-rephotos");
            setPhotoAdapter(getGridView(), album);
        }
        getSwipeRefreshLayout().setRefreshing(false);
    }

    private Upload getUpload(String fileName) {
        String uploadJsonString = getContent(fileName);
        if (isBlank(uploadJsonString)) return null;

        JsonObject uploadJson = new JsonParser().parse(new JsonReader(new StringReader(uploadJsonString))).getAsJsonObject();
        return new Upload(uploadJson);
    }

    @Override
    protected String getPlaceholderString() {
        return getString(R.string.no_local_rephotos);
    }

    private String getContent(String fileName) {
        FileInputStream in = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            in = getContext().openFileInput(fileName);
            inputStreamReader = new InputStreamReader(in);
            bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "Could not read upload file", e);
            return null;
        } finally {
            try {
                 if (in != null) in.close();
                 if (inputStreamReader != null) inputStreamReader.close();
                 if (bufferedReader != null) bufferedReader.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close resources", e);
            }
        }
    }
}
