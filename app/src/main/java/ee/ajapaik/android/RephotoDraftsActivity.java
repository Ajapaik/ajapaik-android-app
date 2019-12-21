package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import ee.ajapaik.android.fragment.RephotoDraftsFragment;
import ee.ajapaik.android.fragment.util.AlertFragment;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class RephotoDraftsActivity extends AlbumActivity {
    private static final String TAG = "RephotoDraftsActivity";

    private static final String TAG_FRAGMENT = "fragment";
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 6003;
    private static final int DIALOG_STORAGE_NOT_PERMITTED = 5;

    public static void start(Context context) {
        Log.d(TAG, "start");

        Intent intent = new Intent(context, RephotoDraftsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public RephotoDraftsFragment createFragment(){
        Log.d(TAG, "createFragment");

        return new RephotoDraftsFragment();
    }

    @Override
    protected RephotoDraftsFragment getFragment() {
        Log.d(TAG, "getFragment");

        return (RephotoDraftsFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");

        super.onStart();
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");

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
        Log.d(TAG, "createDialogFragment");

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
        Log.d(TAG, "onDialogFragmentCancelled");

        if (requestCode == DIALOG_STORAGE_NOT_PERMITTED) {
            startNearestActivity();
        }
    }

    private void startNearestActivity() {
        Log.d(TAG, "startNearestActivity");

        this.startActivity(new Intent(this, NearestActivity.class));
    }
}