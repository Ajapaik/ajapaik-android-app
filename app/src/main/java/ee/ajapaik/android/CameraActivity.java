package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;

import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.fragment.CameraFragment;
import ee.ajapaik.android.util.Settings;

public class CameraActivity extends TutorialActivity {
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
        waitForMenuToBeCreatedAndShowTutorial();
        return true;
    }

    public float[] getOrientation() {
        return m_connection.getOrientation();
    }
}
