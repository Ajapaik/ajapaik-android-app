package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;

import ee.ajapaik.android.fragment.LocalRephotosFragment;
import ee.ajapaik.android.fragment.util.AlertFragment;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class LocalRephotosActivity extends AlbumActivity {

    private static final String TAG_FRAGMENT = "fragment";
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 6003;
    private static final int DIALOG_STORAGE_NOT_PERMITTED = 5;

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
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFragment().refresh();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getFragment().refresh();
                } else {
                    showDialogFragment(DIALOG_STORAGE_NOT_PERMITTED);
                }
            }
        }
    }


    @Override
    public DialogFragment createDialogFragment(int requestCode) {
        if(requestCode == DIALOG_STORAGE_NOT_PERMITTED) {
            return AlertFragment.create(
                    getString(R.string.storage_not_permitted_title),
                    getString(R.string.storage_not_permitted_message),
                    getString(R.string.storage_not_permitted_cancel),
                    getString(R.string.storage_not_permitted_open_settings));
        }

        return super.createDialogFragment(requestCode);
    }

    @Override
    public void onDialogFragmentDismissed(DialogFragment fragment, int requestCode, int resultCode) {
        if(requestCode == DIALOG_STORAGE_NOT_PERMITTED) {
            if(resultCode == AlertFragment.RESULT_POSITIVE) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } else {
                startNearestActivity();
            }
        }
    }

    @Override
    public void onDialogFragmentCancelled(DialogFragment fragment, int requestCode) {
        if (requestCode == DIALOG_STORAGE_NOT_PERMITTED) {
            startNearestActivity();
        }
    }

    private void startNearestActivity() {
        this.startActivity(new Intent(this, NearestActivity.class));
    }
}