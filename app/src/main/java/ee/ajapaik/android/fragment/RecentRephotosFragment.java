package ee.ajapaik.android.fragment;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;

import ee.ajapaik.android.R;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.util.WebAction;

public class RecentRephotosFragment extends AlbumFragment {

    @Override
    protected WebAction<Album> createAction(Context context) {
        return Album.createRecentRephotosAction(context, getSettings().getLocation());
    }

    @Override
    protected WebAction createSearchAction(String query) {
        return Album.createRephotoSearchAction(getActivity(), query);
    }

    @Override
    protected String getPlaceholderString() {
        return getString(R.string.no_rephotos);
    }

    @Override
    public String getAlbumTitle() {
        return getString(R.string.recent_rephotos_title);
    }

    @Override
    protected void refresh() {
        getSwipeRefreshLayout().setRefreshing(true);
        super.refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        TODO Remove this method to enable searching from recent rephotos
    }
}
