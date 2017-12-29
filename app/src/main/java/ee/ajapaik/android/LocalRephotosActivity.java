package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;

import ee.ajapaik.android.fragment.LocalRephotosFragment;

public class LocalRephotosActivity extends AlbumActivity {

    private static final String TAG_FRAGMENT = "fragment";

    public static void start(Context context) {
        Intent intent = new Intent(context, LocalRephotosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public LocalRephotosFragment createFragment(){
        return new LocalRephotosFragment();
    }

    protected LocalRephotosFragment getFragment() {
        return (LocalRephotosFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getFragment().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}