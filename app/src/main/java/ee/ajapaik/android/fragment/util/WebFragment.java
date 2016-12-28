package ee.ajapaik.android.fragment.util;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import ee.ajapaik.android.WebService;
import ee.ajapaik.android.util.Settings;
import ee.ajapaik.android.util.WebActivity;

public abstract class WebFragment extends Fragment {
    protected ActionBar getActionBar() {
        return ((WebActivity)getActivity()).getSupportActionBar();
    }

    protected WebService.Connection getConnection() {
        return ((WebActivity)getActivity()).getConnection();
    }

    protected Settings getSettings() {
        return ((WebActivity)getActivity()).getSettings();
    }

    protected boolean checkPlayServices(boolean ui) {
        return ((WebActivity)getActivity()).checkPlayServices(ui);
    }

    protected void registerDevice(boolean ui) {
        ((WebActivity)getActivity()).registerDevice(ui);
    }

    protected void unregisterDevice() {
        ((WebActivity)getActivity()).unregisterDevice();
    }

    protected void showDialog(int dialogId) {
        ((WebActivity)getActivity()).showDialogFragment(dialogId);
    }

    protected void hideDialog(int dialogId) {
        ((WebActivity)getActivity()).hideDialogFragment(dialogId);
    }

    protected void signInWithFacebook() {
        ((WebActivity)getActivity()).signInWithFacebook();
    }

    protected void signInWithGoogle() {
        ((WebActivity)getActivity()).signInWithGoogle();
    }

    protected void signInWithUsername() {
        ((WebActivity)getActivity()).signInWithUsername();
    }

    protected void signOut() {
        ((WebActivity)getActivity()).signOut();
    }

    public DialogFragment createDialogFragment(int requestCode) {
        return null;
    }

    public void onAuthorizationChanged() {
    }
}
