package ee.ajapaik.android.fragment.util;

import androidx.fragment.app.DialogFragment;

public interface DialogInterface {
    void onDialogFragmentDismissed(DialogFragment fragment, int requestCode, int resultCode);
    void onDialogFragmentCancelled(DialogFragment fragment, int requestCode);
}
