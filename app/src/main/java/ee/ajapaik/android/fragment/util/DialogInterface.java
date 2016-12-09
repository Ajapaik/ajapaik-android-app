package ee.ajapaik.android.fragment.util;

import android.support.v4.app.DialogFragment;

public interface DialogInterface {
    void onDialogFragmentDismissed(DialogFragment fragment, int requestCode, int resultCode);
    void onDialogFragmentCancelled(DialogFragment fragment, int requestCode);
}
