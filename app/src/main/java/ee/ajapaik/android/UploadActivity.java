package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import ee.ajapaik.android.data.Upload;
import ee.ajapaik.android.fragment.CameraFragment;
import ee.ajapaik.android.fragment.UploadFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.WebActivity;

public class UploadActivity extends WebActivity {
    private static final String EXTRA_UPLOAD = "upload";

    private static final String TAG_FRAGMENT = "fragment";

    public static Intent getStartIntent(Context context, Upload upload) {
        Intent intent = new Intent(context, UploadActivity.class);

        intent.putExtra(EXTRA_UPLOAD, upload);

        return intent;
    }

    public static void start(Context context, Upload upload) {
        context.startActivity(getStartIntent(context, upload));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            Upload upload = getIntent().getParcelableExtra(EXTRA_UPLOAD);
            UploadFragment fragment = new UploadFragment();

            fragment.setUpload(upload);

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment, TAG_FRAGMENT).commit();
        }
    }

    protected CameraFragment getFragment() {
        return (CameraFragment)getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
    }
}
