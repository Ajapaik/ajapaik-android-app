package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.Upload;
import ee.ajapaik.android.fragment.CameraFragment;
import ee.ajapaik.android.fragment.UploadFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.Settings;
import ee.ajapaik.android.util.WebActivity;

public class CameraActivity extends WebActivity {
    private static final String EXTRA_PHOTO = "photo";

    private static final String TAG_FRAGMENT = "fragment";

    public static Intent getStartIntent(Context context, Photo photo) {
        Intent intent = new Intent(context, CameraActivity.class);

        intent.putExtra(EXTRA_PHOTO, photo);

        return intent;
    }

    public static void start(Context context, Photo photo) {
        context.startActivity(getStartIntent(context, photo));
    }

    private static final int MIN_DISTANCE_IN_METERS = 1;

    private final LocationService.Connection m_connection = new LocationService.Connection() {
        public void onLocationChanged(Location newLocation) {
            Settings settings = getSettings();
            Location oldLocation = settings.getLocation();

            if(oldLocation == null || oldLocation.distanceTo(newLocation) > MIN_DISTANCE_IN_METERS) {
                settings.setLocation(newLocation);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            Photo photo = getIntent().getParcelableExtra(EXTRA_PHOTO);
            CameraFragment fragment = new CameraFragment();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_camera, menu);

        return true;
    }


    public void showUploadPreview(final Upload upload) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_upload);
                findViewById(R.id.action_flip).setVisibility(View.INVISIBLE);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                UploadFragment fragment = new UploadFragment();

                fragment.setUpload(upload);

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                getSupportFragmentManager().beginTransaction().add(R.id.container, fragment, TAG_FRAGMENT).commit();
            }
        });
    }

    public float[] getOrientation() {
        return m_connection.getOrientation();
    }

    protected CameraFragment getFragment() {
        return (CameraFragment)getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
    }
}
