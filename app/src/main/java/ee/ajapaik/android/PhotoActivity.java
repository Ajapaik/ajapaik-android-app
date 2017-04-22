package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.fragment.PhotoFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.Settings;
import ee.ajapaik.android.util.WebActivity;

public class PhotoActivity extends WebActivity {
    private static final String EXTRA_ALBUM = "album";
    private static final String EXTRA_PHOTO = "photo";

    private static final String TAG_FRAGMENT = "fragment";

    private static final int MIN_DISTANCE_IN_METERS = 1;

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    public static Intent getStartIntent(Context context, Photo photo, Album album) {
        Intent intent = new Intent(context, PhotoActivity.class);

        intent.putExtra(EXTRA_PHOTO, photo);
        intent.putExtra(EXTRA_ALBUM, album);

        return intent;
    }

    public static void start(Context context, Photo photo, Album album) {
        context.startActivity(getStartIntent(context, photo, album));
    }

    private final LocationService.Connection m_connection = new LocationService.Connection() {
        public void onLocationChanged(Location newLocation) {
            Settings settings = getSettings();
            Location oldLocation = settings.getLocation();

            if(oldLocation == null || oldLocation.distanceTo(newLocation) > MIN_DISTANCE_IN_METERS) {
                PhotoFragment fragment = getFragment();

                settings.setLocation(newLocation);

                if(fragment != null) {
                    fragment.invalidate(newLocation, getOrientation());
                }
            }
        }

        @Override
        public void onOrientationChanged() {
            Location location = getSettings().getLocation();

            if(location != null) {
                PhotoFragment fragment = getFragment();

                if(fragment != null) {
                    fragment.invalidate(location, getOrientation());
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if(savedInstanceState == null) {
            Photo photo = getIntent().getParcelableExtra(EXTRA_PHOTO);
            Album album = getIntent().getParcelableExtra(EXTRA_ALBUM);
            PhotoFragment fragment = new PhotoFragment();

            fragment.setAlbum(album);
            fragment.setPhoto(photo);

            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment, TAG_FRAGMENT).commit();
        }
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
    protected void onStart() {
        super.onStart();
        m_connection.connect(this);
    }

    @Override
    protected void onStop() {
        m_connection.disconnect(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo, menu);

        return true;
    }

    protected PhotoFragment getFragment() {
        return (PhotoFragment)getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        getFragment().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
