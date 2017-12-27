package ee.ajapaik.android.fragment;


import android.os.Bundle;

import java.util.ArrayList;

import ee.ajapaik.android.R;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Photo;

public class LocalRephotosFragment extends PhotosFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refresh();
    }

    @Override
    protected void refresh() {
        Album album = new Album(new ArrayList<Photo>(), "local-rephotos");
        if (album.getPhotos().isEmpty()) {
            initializeEmptyGridView(getGridView());
        } else {
            setPhotoAdapter(getGridView(), album);
        }
        getSwipeRefreshLayout().setRefreshing(false);
    }

    @Override
    protected String getPlaceholderString() {
        return getString(R.string.no_local_rephotos);
    }
}
