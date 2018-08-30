package ee.ajapaik.android.fragment;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import ee.ajapaik.android.R;
import ee.ajapaik.android.data.FinnaAlbum;
import ee.ajapaik.android.data.Album;

import ee.ajapaik.android.util.WebAction;

public class FinnaFragment extends NearestFragment {
    private static final String TAG = "FinnaFragment";
    private static final int DEFAULT_RANGE = 20000;
    private String m_albumTitle="Untitled";
    private String m_finnaAlbum="";

    @Override
    protected WebAction<Album> createAction(Context context) {
        Location location = getSettings().getLocation();
        Log.d(TAG, "FinnaFragment: createAction");
        return (location != null) ? FinnaAlbum.createNearestAction(context, location, null, DEFAULT_RANGE, m_finnaAlbum) : null;
    }

    @Override
    protected WebAction<Album> createSearchAction(String query) {
        Location location = getSettings().getLocation();
        return FinnaAlbum.createSearchAction(getActivity(), query, location, DEFAULT_RANGE, m_finnaAlbum);
    }

    public void setAlbumTitle(String albumTitle) {
        m_albumTitle=albumTitle;
    }

    public void setFinnaAlbum(String finnaAlbum) {
        m_finnaAlbum=finnaAlbum;
    }

    @Override
    public String getAlbumTitle() {
        return m_albumTitle;
    }
}
