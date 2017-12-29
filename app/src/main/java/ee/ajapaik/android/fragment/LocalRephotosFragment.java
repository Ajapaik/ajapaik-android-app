package ee.ajapaik.android.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ee.ajapaik.android.NearestActivity;
import ee.ajapaik.android.R;
import ee.ajapaik.android.UploadActivity;
import ee.ajapaik.android.adapter.PhotoAdapter;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.Upload;
import ee.ajapaik.android.util.ExifService;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static ee.ajapaik.android.UploadActivity.CreatedFrom.LOCAL_REPHOTOS;
import static ee.ajapaik.android.util.ExifService.USER_COMMENT;
import static org.apache.http.util.TextUtils.isBlank;

public class LocalRephotosFragment extends PhotosFragment {

    private static final String TAG = "LocalRephotosFragment";
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 6003;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION);
        } else {
            refresh();
        }
    }

    @Override
    protected void refresh() {
        File[] images = Upload.getFolder().listFiles();

        List<Photo> photos = new ArrayList<>();
        final Map<Photo, Upload> uploadsByPhoto = new HashMap<>();

        for (File file : images) {
            Upload upload = getUpload(file);
            if (upload == null) continue;
            photos.add(upload.getPhoto());
            uploadsByPhoto.put(upload.getPhoto(), upload);
        }

        if (photos.isEmpty()) {
            initializeEmptyGridView(getGridView());
        } else {
            Album album = new Album(photos, "local-rephotos");
            setPhotoAdapter(getGridView(), album.getPhotos(), new PhotoAdapter.OnPhotoSelectionListener() {
                @Override
                public void onSelect(Photo photo) {
                    UploadActivity.start(getActivity(), uploadsByPhoto.get(photo), LOCAL_REPHOTOS);
                }
            });
        }
        getSwipeRefreshLayout().setRefreshing(false);
    }

    private Upload getUpload(File file) {
        String uploadJsonString = getUploadData(file);
        if (isBlank(uploadJsonString)) return null;

        JsonObject uploadJson = new JsonParser().parse(new JsonReader(new StringReader(uploadJsonString))).getAsJsonObject();
        return new Upload(uploadJson);
    }

    @Override
    protected String getPlaceholderString() {
        return getString(R.string.no_local_rephotos);
    }

    private String getUploadData(File file) {
        return ExifService.readField(file, USER_COMMENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    refresh();
                } else {
                    this.startActivity(new Intent(getActivity(), NearestActivity.class));
                }
            }
        }
    }
}
