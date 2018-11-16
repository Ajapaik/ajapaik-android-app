package ee.ajapaik.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.fragment.PhotoFragment;
import ee.ajapaik.android.util.Settings;
import ee.ajapaik.android.util.WebActivity;

public class PhotoActivity extends WebActivity {
    public static final String IS_FAVORITED_KEY = "isFavorited";
    public static final String PHOTO_IDENTIFIER_KEY = "photoIdentifier";

    private static final String EXTRA_ALBUM = "album";
    private static final String EXTRA_PHOTO = "photo";

    private static final String TAG_FRAGMENT = "fragment";

    private static final int MIN_DISTANCE_IN_METERS = 1;

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
                    fragment.invalidate(newLocation);
                }
            }
        }

        @Override
        public void onOrientationChanged() {
            Location location = getSettings().getLocation();

            if(location != null) {
                PhotoFragment fragment = getFragment();

                if(fragment != null) {
                    fragment.invalidate(location);
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(IS_FAVORITED_KEY, getFragment().isFavorited());
        intent.putExtra(PHOTO_IDENTIFIER_KEY, getFragment().getPhoto().getIdentifier());
        setResult(Activity.RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    protected void onStart() {
        super.onStart();
        m_connection.connect(this);
    }

    @Override
    protected void onStop() {
        m_connection.disconnect(this);
        super.onStop();
    }

    protected PhotoFragment getFragment() {
        return (PhotoFragment)getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        getFragment().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
