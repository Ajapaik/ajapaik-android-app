package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.fragment.AlbumFragment;
import ee.ajapaik.android.fragment.PhotosFragment;

public class AlbumActivity extends NavigationDrawerActivity {
    private static final String TAG = "AlbumActivity";

    private static final String TAG_FRAGMENT = "fragment";

    private static final String EXTRA_ALBUM_ID = "album_id";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_TYPE = "type";

    public static Intent getStartIntent(Context context, String albumId, String title, String type) {
        Log.d(TAG, "AlbumActivity: getStartIntent() " + type);


        Intent intent = new Intent(context, AlbumActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra(EXTRA_ALBUM_ID, albumId);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_TYPE, type);


        return intent;
    }

    public static void start(Context context, Album album) {
        Log.d(TAG, "AlbumActivity: start() " + album.getType());

        context.startActivity(getStartIntent(context, album.getIdentifier(), album.getTitle(), album.getType()));
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_album);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, createFragment(), TAG_FRAGMENT).commit();
        }
    }

    protected PhotosFragment createFragment() {
        AlbumFragment fragment = new AlbumFragment();

        fragment.setAlbumIdentifier(getIntent().getStringExtra(EXTRA_ALBUM_ID));
        fragment.setAlbumType(getIntent().getStringExtra(EXTRA_TYPE));


        return fragment;
    }

    protected PhotosFragment getFragment() {
        return (PhotosFragment)getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
    }
}
