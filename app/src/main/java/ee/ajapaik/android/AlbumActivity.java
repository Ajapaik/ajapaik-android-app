package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.fragment.AlbumFragment;
import ee.ajapaik.android.test.R;

public class AlbumActivity extends NavigationDrawerActivity {
    private static final String TAG_FRAGMENT = "fragment";

    private static final String EXTRA_ALBUM_ID = "album_id";
    private static final String EXTRA_TITLE = "title";

    public static Intent getStartIntent(Context context, String albumId, String title) {
        Intent intent = new Intent(context, AlbumActivity.class);

        intent.putExtra(EXTRA_ALBUM_ID, albumId);
        intent.putExtra(EXTRA_TITLE, title);

        return intent;
    }

    public static void start(Context context, Album album) {
        context.startActivity(getStartIntent(context, album.getIdentifier(), album.getTitle()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album);
        configureNavigationDrawer();
        configureToolbar();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, createFragment(), TAG_FRAGMENT).commit();
        }
    }

    protected AlbumFragment createFragment() {
        AlbumFragment fragment = new AlbumFragment();

        fragment.setAlbumIdentifier(getIntent().getStringExtra(EXTRA_ALBUM_ID));

        return fragment;
    }

    protected AlbumFragment getFragment() {
        return (AlbumFragment)getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
    }
}
