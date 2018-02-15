package ee.ajapaik.android.fragment;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import static ee.ajapaik.android.UploadActivity.CreatedFrom.REPHOTOS;
import static ee.ajapaik.android.util.ExifService.USER_COMMENT;
import static org.apache.http.util.TextUtils.isBlank;

public class RephotoDraftsFragment extends PhotosFragment {

    private static final String TAG = "RephotoDraftsFragment";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getSwipeRefreshLayout().setEnabled(false);
        refresh();
    }

    @Override
    protected WebAction createSearchAction(String query) {
        return Album.createRephotoSearchAction(getActivity(), query);
    }

    @Override
    public void refresh() {
        if (ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
            return;

        File[] images = Upload.getFolder().listFiles();

        final Map<Photo, Upload> uploadsByPhoto = new LinkedHashMap<>();

        for (File file : images) {
            Upload upload = getUpload(file);
            if (upload == null) continue;
            uploadsByPhoto.put(upload.getPhoto(), upload);
        }

        if (uploadsByPhoto.isEmpty()) {
            initializeEmptyGridView(getGridView());
        } else {
            Album album = new Album(new ArrayList<>(uploadsByPhoto.keySet()), "rephoto-drafts");
            setPhotoAdapter(getGridView(), album.getPhotos(), new PhotoAdapter.OnPhotoSelectionListener() {
                @Override
                public void onSelect(Photo photo) {
                    UploadActivity.start(getActivity(), uploadsByPhoto.get(photo), REPHOTOS);
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
        return getString(R.string.no_rephotos);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        TODO: Remove this method to enable searching user rephotos
    }
}
