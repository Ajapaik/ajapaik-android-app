package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.Upload;
import ee.ajapaik.android.fragment.CameraFragment;
import ee.ajapaik.android.fragment.UploadFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.Settings;
import ee.ajapaik.android.util.WebActivity;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

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
        tutorial();
    }

    private void tutorial() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "ThisValueIsStoredAndTutorialIsContinuedWhereLeftPreviousTime");

        sequence.setConfig(config);

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.image))
                .setTitleText("Hide old photo")
                .setContentText("Tap on old photo")
                .setDismissText("Got it")
                .setTargetTouchable(true)
                .setFadeDuration(500)
                .setShapePadding(-500)
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.image))
                .setTitleText("Show old photo")
                .setContentText("Tap on old photo")
                .setDismissText("Got it")
                .setTargetTouchable(true)
                .setShapePadding(-500)
                .setFadeDuration(500)
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.image))
                .setTitleText("Change transparency")
                .setContentText("Swipe left or right to change old photo opacity")
                .setDismissText("Got it")
                .setTargetTouchable(true)
                .setFadeDuration(2000)
                .setShapePadding(-350)
                .withRectangleShape()
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.button_action_camera))
                .setTitleText("Take rephoto")
                .setContentText("Tap this icon to take rephoto. Alternatively you can hold your finger anywhere on the screen to take rephoto")
                .setDismissText("Got it")
                .setTargetTouchable(true)
                .build());

        sequence.start();
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

                UploadFragment fragment = new UploadFragment();

                fragment.setUpload(upload);

                FragmentManager supportFragmentManager = getSupportFragmentManager();
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment, TAG_FRAGMENT).commit();
                supportFragmentManager.executePendingTransactions();
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
