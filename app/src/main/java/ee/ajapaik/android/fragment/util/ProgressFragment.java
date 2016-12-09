package ee.ajapaik.android.fragment.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;

import ee.ajapaik.android.test.BuildConfig;

public class ProgressFragment extends DialogFragment {
    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";

    public static ProgressFragment create(String title) {
        return create(title, null);
    }

    public static ProgressFragment create(String title, String message) {
        ProgressFragment fragment = new ProgressFragment();
        Bundle arguments = new Bundle();

        arguments.putString(KEY_TITLE, title);
        arguments.putString(KEY_MESSAGE, message);

        fragment.setArguments(arguments);
        fragment.setCancelable(false);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        Bundle arguments = getArguments();
        String title = arguments.getString(KEY_TITLE);
        String message = arguments.getString(KEY_MESSAGE);

        if(title != null) {
            dialog.setTitle(title);
        }

        if(message != null) {
            dialog.setMessage(message);
        }

        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    try {
                        ((ee.ajapaik.android.fragment.util.DialogInterface)getActivity()).onDialogFragmentCancelled(ProgressFragment.this, getTargetRequestCode());
                    }
                    catch(Exception e) {
                        if(BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    dialog.dismiss();

                    return true;
                }

                return false;
            }
        });

        return dialog;
    }
}
