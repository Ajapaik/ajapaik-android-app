package ee.ajapaik.android.fragment.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;

import ee.ajapaik.android.BuildConfig;

public class AlertFragment extends DialogFragment {
    public static final int RESULT_NEGATIVE = -1;
    public static final int RESULT_POSITIVE = 0;
    public static final int RESULT_NEUTRAL = 1;

    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_ICON = "icon";
    private static final String KEY_CANCEL_BUTTON = "cancel";
    private static final String KEY_OTHER_BUTTONS = "other";
    private static final String KEY_USER_INFO = "info";

    private static final int INVALID_RESOURCE_ID = -1;

    public static AlertFragment create(String title, String message, String cancelButton) {
        return create(title, message, cancelButton, (String[])null);
    }

    public static AlertFragment create(String title, String message, String cancelButton, String... otherButtons) {
        AlertFragment fragment = new AlertFragment();
        Bundle arguments = new Bundle();

        arguments.putString(KEY_TITLE, title);
        arguments.putString(KEY_MESSAGE, message);
        arguments.putString(KEY_CANCEL_BUTTON, cancelButton);

        if(otherButtons != null && otherButtons.length > 0) {
            arguments.putStringArray(KEY_OTHER_BUTTONS, otherButtons);
        }

        fragment.setArguments(arguments);
        fragment.setCancelable(false);

        return fragment;
    }

    public <T extends Parcelable> T getUserInfo() {
        return getArguments().getParcelable(KEY_USER_INFO);
    }

    public AlertFragment setUserInfo(Parcelable userInfo) {
        getArguments().putParcelable(KEY_USER_INFO, userInfo);

        return this;
    }

    public AlertFragment setIcon(int iconId) {
        getArguments().putInt(KEY_ICON, iconId);

        return this;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        int iconId = arguments.getInt(KEY_ICON, INVALID_RESOURCE_ID);
        String title = arguments.getString(KEY_TITLE);
        String message = arguments.getString(KEY_MESSAGE);
        String cancelButton = arguments.getString(KEY_CANCEL_BUTTON);
        String[] otherButtons = arguments.getStringArray(KEY_OTHER_BUTTONS);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    int result = (which == AlertDialog.BUTTON_NEGATIVE) ? RESULT_NEGATIVE : ((which == AlertDialog.BUTTON_POSITIVE) ? RESULT_POSITIVE : RESULT_NEUTRAL);

                    ((ee.ajapaik.android.fragment.util.DialogInterface)getActivity()).onDialogFragmentDismissed(AlertFragment.this, getTargetRequestCode(), result);
                }
                catch(Exception e) {
                    if(BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }

                dialog.dismiss();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog;

        if(iconId != INVALID_RESOURCE_ID) {
            builder.setIcon(iconId);
        }

        if(title != null) {
            builder.setTitle(title);
        }

        if(message != null) {
            builder.setMessage(message);
        }

        if(cancelButton != null) {
            builder.setNegativeButton(cancelButton, listener);
        }

        if(otherButtons != null) {
            if(otherButtons.length > 0 && otherButtons[0] != null) {
                builder.setPositiveButton(otherButtons[0], listener);
            }

            if(otherButtons.length > 1 && otherButtons[1] != null) {
                builder.setNeutralButton(otherButtons[1], listener);
            }
        }

        dialog = builder.create();
        dialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    try {
                        ((ee.ajapaik.android.fragment.util.DialogInterface)getActivity()).onDialogFragmentCancelled(AlertFragment.this, getTargetRequestCode());
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
