package ee.ajapaik.android.fragment.util;

import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import ee.ajapaik.android.R;
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

    protected void showDialog(int dialogId) {
        ((WebActivity)getActivity()).showDialogFragment(dialogId);
    }

    protected void hideDialog() {
        ((WebActivity)getActivity()).hideDialogFragment();
    }

    protected void showRequestErrorToast() {
        Toast.makeText(getActivity(), getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
    }

    protected void signInWithFacebook() {
        ((WebActivity)getActivity()).signInWithFacebook();
    }

    protected void signInWithGoogle() {
        ((WebActivity)getActivity()).signInWithGoogle();
    }

    protected void registerWithUsername() {
        ((WebActivity)getActivity()).registerWithUsername();
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
