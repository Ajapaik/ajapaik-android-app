package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.fragment.AlbumFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.WebActivity;

public class AlbumActivity extends WebActivity {
    private static final String TAG_FRAGMENT = "fragment";

    private static final String EXTRA_ALBUM_ID = "album_id";
    private static final String EXTRA_TITLE = "title";

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

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

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, createFragment(), TAG_FRAGMENT).commit();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album, menu);

        return true;
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
