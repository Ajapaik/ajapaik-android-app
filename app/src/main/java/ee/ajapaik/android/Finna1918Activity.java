package ee.ajapaik.android;

import android.util.Log;

import ee.ajapaik.android.fragment.AlbumFragment;
import ee.ajapaik.android.fragment.FinnaFragment;


public class Finna1918Activity extends NearestActivity {
    private static final String TAG = "FinnaActivity";

    protected AlbumFragment createFragment() {
        Log.d(TAG, "Finna activity");

        FinnaFragment f =  new FinnaFragment();
        f.setAlbumTitle(getString(R.string.finna1918_title));
        f.setFinnaAlbum("signebrander");
        return f;
    }
}
