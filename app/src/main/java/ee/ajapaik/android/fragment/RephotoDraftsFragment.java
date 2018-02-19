package ee.ajapaik.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ee.ajapaik.android.PhotoActivity;
import ee.ajapaik.android.R;
import ee.ajapaik.android.UploadActivity;
import ee.ajapaik.android.adapter.DraftAdapter;
import ee.ajapaik.android.adapter.PhotoAdapter;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.PhotoDraftsDTO;
import ee.ajapaik.android.data.Upload;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.RephotoDraftService;
import ee.ajapaik.android.util.WebAction;
import ee.ajapaik.android.widget.StaggeredGridView;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static ee.ajapaik.android.UploadActivity.CreatedFrom.REPHOTOS;

public class RephotoDraftsFragment extends PhotosFragment {

    private static final String TAG = "RephotoDraftsFragment";

    private String m_searchQuery;
    private RephotoDraftService m_rephotoDraftService;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        m_rephotoDraftService = new RephotoDraftService();
        getSwipeRefreshLayout().setEnabled(false);
        refresh();
    }

    @Override
    protected WebAction createSearchAction(String query) {
        this.m_searchQuery = query;
        return null;
    }

    @Override
    public void refresh() {
        if (ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
            return;

        final Map<String, List<Upload>> uploadsByPhoto = m_rephotoDraftService.getAllDrafts(m_searchQuery);

        if (uploadsByPhoto.isEmpty()) {
            initializeEmptyGridView(getGridView());
        } else {
            List<PhotoDraftsDTO> photos = new ArrayList<>();

            for (String identifier : uploadsByPhoto.keySet()) {
                List<Upload> uploads = uploadsByPhoto.get(identifier);
                photos.add(new PhotoDraftsDTO(uploads.get(0).getPhoto(), uploads.size()));
            }

            getGridView().setAdapter(new DraftAdapter(getGridView().getContext(), photos,
                    getSettings().getLocation(), new DraftAdapter.OnPhotoSelectionListener() {
                @Override
                public void onSelect(Photo photo) {
                    UploadActivity.start(getActivity(), uploadsByPhoto.get(photo.getIdentifier()), REPHOTOS);
                }
            }));
        }
        handleLoadingFinished();
    }

    @Override
    protected void performAction(Context context, WebAction action) {
        refresh();
        m_searchQuery = null;
    }

    @Override
    protected String getPlaceholderString() {
        return getString(R.string.no_rephotos);
    }

//    TODO this can be simplified?
    @Override
    public void setAlbum(Album album) {
        if(!Objects.match(m_album, album)) {
            StaggeredGridView gridView = getGridView();

            m_album = album;

            if(m_album != null && m_album.getPhotos().size() > 0) {
                getEmptyView().setText("");
                setPhotoAdapter(gridView, new PhotoAdapter.OnPhotoSelectionListener() {
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
}
