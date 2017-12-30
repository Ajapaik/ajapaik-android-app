package ee.ajapaik.android.fragment;

import android.os.Bundle;
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

import ee.ajapaik.android.PhotoActivity;
import ee.ajapaik.android.R;
import ee.ajapaik.android.UploadActivity;
import ee.ajapaik.android.adapter.PhotoAdapter;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.Upload;
import ee.ajapaik.android.util.ExifService;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebAction;
import ee.ajapaik.android.widget.StaggeredGridView;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static ee.ajapaik.android.UploadActivity.CreatedFrom.LOCAL_REPHOTOS;
import static ee.ajapaik.android.util.ExifService.USER_COMMENT;
import static org.apache.http.util.TextUtils.isBlank;

public class LocalRephotosFragment extends PhotosFragment {

    private static final String TAG = "LocalRephotosFragment";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refresh();
    }

    @Override
    protected WebAction createSearchAction(String query) {
        return Album.createSearchAction(getActivity(), query);
    }

    @Override
    public void refresh() {
        if (ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
            return;

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

    @Override
    public void setAlbum(Album album) {
        if(!Objects.match(m_album, album)) {
            StaggeredGridView gridView = getGridView();

            m_album = album;

            if(m_album != null && m_album.getPhotos().size() > 0) {
                getEmptyView().setText("");
                setPhotoAdapter(gridView, m_album.getPhotos(), new PhotoAdapter.OnPhotoSelectionListener() {
                    @Override
                    public void onSelect(Photo photo) {
                        PhotoActivity.start(getActivity(), photo, m_album);
                    }
                });
            } else {
                initializeEmptyGridView(gridView);
            }
        }
    }

    private String getUploadData(File file) {
        return ExifService.readField(file, USER_COMMENT);
    }
}
