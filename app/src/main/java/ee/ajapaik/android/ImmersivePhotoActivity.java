package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.fragment.ImmersivePhotoFragment;
import ee.ajapaik.android.util.WebActivity;

public class ImmersivePhotoActivity extends WebActivity {
    private static final String TAG_FRAGMENT = "fragment";
    private static final String EXTRA_PHOTO = "photo";

    public static Intent getStartIntent(Context context, Photo photo) {
        Intent intent = new Intent(context, ImmersivePhotoActivity.class);
        intent.putExtra(EXTRA_PHOTO, photo);
        return intent;
    }

    public static void start(Context context, Photo photo) {
        context.startActivity(getStartIntent(context, photo));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_immersive_photo);
        if (savedInstanceState == null) {
            Photo photo = getIntent().getParcelableExtra(EXTRA_PHOTO);
            ImmersivePhotoFragment fragment = new ImmersivePhotoFragment();

            fragment.setPhoto(photo);

            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment, TAG_FRAGMENT).commit();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
