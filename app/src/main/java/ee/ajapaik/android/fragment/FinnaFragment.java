package ee.ajapaik.android.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ee.ajapaik.android.R;
import ee.ajapaik.android.data.FinnaAlbum;
import ee.ajapaik.android.data.Album;

import ee.ajapaik.android.util.WebAction;

public class FinnaFragment extends NearestFragment {
    private static final String TAG = "FinnaFragment";
    private static final int DEFAULT_RANGE = 20000;
    private static final String KEY_TITLE = "title";
    private static final String KEY_FINNA_ALBUM = "finna_album";

    private String m_albumTitle="Untitled";
    private String m_finnaAlbum="";

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        Log.d(TAG, "FinnaFragment: onSaveInstanceState " + m_albumTitle);

        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(KEY_TITLE, m_albumTitle);
        savedInstanceState.putString(KEY_FINNA_ALBUM, m_finnaAlbum);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            m_albumTitle = savedInstanceState.getString(KEY_TITLE);
            m_finnaAlbum = savedInstanceState.getString(KEY_FINNA_ALBUM);
            if(super.getAlbum() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getAlbumTitle());
            }
        }
    }

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
        Log.d(TAG, "FinnaFragment: setAlbumTitle " + albumTitle);

        m_albumTitle=albumTitle;
    }

    public void setFinnaAlbum(String finnaAlbum) {
        m_finnaAlbum=finnaAlbum;
    }

    @Override
    public String getAlbumTitle() {
        Log.d(TAG, "FinnaFragment: getAlbumTitle " + m_albumTitle);

        return m_albumTitle;
    }
}
