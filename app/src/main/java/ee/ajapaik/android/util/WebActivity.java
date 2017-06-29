package ee.ajapaik.android.util;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import ee.ajapaik.android.ProfileActivity;
import ee.ajapaik.android.WebService;
import ee.ajapaik.android.data.Profile;
import ee.ajapaik.android.data.Session;
import ee.ajapaik.android.fragment.util.DialogInterface;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.test.R;

import java.io.IOException;
import java.util.Arrays;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static ee.ajapaik.android.ProfileActivity.LAST_ACTIVITY;
import static ee.ajapaik.android.util.Authorization.Type.FACEBOOK;
import static ee.ajapaik.android.util.Authorization.Type.GOOGLE;

public class WebActivity extends AppCompatActivity implements DialogInterface, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "WebActivity";

    private static final String DIALOG_PREFIX = "dialog_";

    private WebService.Connection m_connection = new WebService.Connection();
    protected Settings m_settings;

    public static final int FACEBOOK_SIGN_IN_RESOLUTION_REQUEST = 9001;
    private static final int GOOGLE_SIGN_IN_RESOLUTION_REQUEST = 9002;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9003;
    private static final int GET_ACCOUNTS_PERMISSION = 6001;

    private static final String SERVER_ID = "";
    private static final String SENDER_ID = "";
    private CallbackManager m_facebookCallback = null;
    private GoogleApiClient m_googleApiClient = null;
    private boolean m_isResolving = false;
    private boolean m_shouldResolve = false;

    private ProgressDialog progressDialog;

    public WebService.Connection getConnection() {
        return m_connection;
    }

    public Settings getSettings() {
        return m_settings;
    }

    public void signInWithFacebook() {
        if (m_facebookCallback == null) {
            m_facebookCallback = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(m_facebookCallback, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    if (getSettings().getAuthorization().getType() != FACEBOOK) {
                        progressDialog = ProgressDialog.show(WebActivity.this, "Logging in...", "Please wait...");
                    }
                    Authorization authorization = new Authorization(FACEBOOK, loginResult.getAccessToken().getUserId(), loginResult.getAccessToken().getToken());
                    getSettings().setAuthorization(authorization);

                    getConnection().enqueue(WebActivity.this, Session.createLoginAction(WebActivity.this, authorization), new WebAction.ResultHandler<Session>() {
                        @Override
                        public void onActionResult(ee.ajapaik.android.data.util.Status status, Session session) {
                            if (session != null) {
                                login(session);
                            } else {
                                dismissProgressDialog();
                            }
                        }
                    });
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException e) {
                    Log.d(TAG, "onError " + e);
                }
            });
        }

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
    }

    public void signInWithUsername() {
        Authorization authorization = getSettings().getAuthorization();
        getConnection().enqueue(WebActivity.this, Session.createLoginAction(WebActivity.this, authorization), new WebAction.ResultHandler<Session>() {
            @Override
            public void onActionResult(ee.ajapaik.android.data.util.Status status, Session session) {
                if (session != null) {
                    login(session);
                } else {
                    findViewById(R.id.login_unsuccessful).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.input_password)).setText("");
                }
            }
        });
    }

    private void login(Session session) {
        dismissProgressDialog();
        m_settings.setSession(session);
        getSettings().setProfile(new Profile(getSettings().getSession().getAttributes()));
        String lastActivity = getIntent().getStringExtra(LAST_ACTIVITY);
        finish();
        if (!"upload".equals(lastActivity)) {
            ProfileActivity.start(WebActivity.this, "login");
            this.overridePendingTransition(0, 0);
        }
    }

    private void logout() {
        dismissProgressDialog();
        m_settings.setSession(null);
        getSettings().setProfile(new Profile());
        finish();
        ProfileActivity.start(WebActivity.this, "login");
        this.overridePendingTransition(0, 0);
    }

    public void signInWithGoogle() {
        if (ContextCompat.checkSelfPermission(this, GET_ACCOUNTS) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{GET_ACCOUNTS}, GET_ACCOUNTS_PERMISSION);
        } else {
            connectToGoogleApi();
        }
    }

    private void connectToGoogleApi() {
        if (m_googleApiClient == null) {
            m_googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addScope(new Scope(Scopes.PROFILE))
                    .build();
        }

        m_shouldResolve = true;
        m_googleApiClient.connect();

        // Show a message to the user that we are signing in.
        //mStatusTextView.setText(R.string.signing_in);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GET_ACCOUNTS_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    connectToGoogleApi();
                }
            }
        }
    }

    public void signOut() {
        progressDialog = ProgressDialog.show(WebActivity.this, "Logging out...", "Please wait...");
        Authorization loggedInAuthorization = m_settings.getAuthorization();
        if (FACEBOOK == loggedInAuthorization.getType()) {
            new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse graphResponse) {
                    LoginManager.getInstance().logOut();
                }
            }).executeAsync();
        } else if (GOOGLE == loggedInAuthorization.getType()) {
            logoutFromGoogle();
        }

        Authorization anonymous = Authorization.getAnonymous(this);
        getSettings().setAuthorization(anonymous);

        getConnection().enqueue(WebActivity.this, Session.createLogoutAction(WebActivity.this), new WebAction.ResultHandler<Session>() {
            @Override
            public void onActionResult(ee.ajapaik.android.data.util.Status status, Session session) {
                logout();
                invalidateAuthorization();
            }
        });
    }

    private void logoutFromGoogle() {
        if (m_googleApiClient == null) {
            m_googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addScope(new Scope(Scopes.PROFILE))
                    .build();
        }
        if (m_googleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(m_googleApiClient);
            m_googleApiClient.disconnect();
        }
    }

    public boolean checkPlayServices(boolean ui) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        }

        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
            if (ui) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
        } else {
            Log.i(TAG, "GCM is not supported for this device.");
        }

        return false;
    }

    public void registerDevice(boolean ui) {
    }

    public void unregisterDevice() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_settings = new Settings(this);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        m_connection.dequeueAll(this);

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (m_googleApiClient != null) {
            m_googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        dismissProgressDialog();
        if (m_googleApiClient != null) {
            m_googleApiClient.disconnect();
        }

        super.onStop();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    }

    @Override
    protected void onPause() {
        dismissProgressDialog();
        super.onPause();
    }

    @Override
    public void onDialogFragmentDismissed(DialogFragment fragment, int requestCode, int resultCode) {
        Object[] fragments = getSupportFragmentManager().getFragments().toArray();

        for (Object f : fragments) {
            if (f != fragment && f instanceof DialogInterface) {
                ((DialogInterface) f).onDialogFragmentDismissed(fragment, requestCode, resultCode);
            }
        }
    }

    @Override
    public void onDialogFragmentCancelled(DialogFragment fragment, int requestCode) {
        Object[] fragments = getSupportFragmentManager().getFragments().toArray();

        for (Object f : fragments) {
            if (f != fragment && f instanceof DialogInterface) {
                ((DialogInterface) f).onDialogFragmentCancelled(fragment, requestCode);
            }
        }
    }

    private void invalidateAuthorization() {
        Object[] fragments = getSupportFragmentManager().getFragments().toArray();

        for (Object fragment : fragments) {
            if (fragment instanceof WebFragment) {
                ((WebFragment) fragment).onAuthorizationChanged();
            }
        }
    }

    protected DialogFragment createDialogFragment(int requestCode) {
        Object[] fragments = getSupportFragmentManager().getFragments().toArray();

        for (Object fragment : fragments) {
            if (fragment instanceof WebFragment) {
                DialogFragment f = ((WebFragment) fragment).createDialogFragment(requestCode);

                if (f != null) {
                    return f;
                }
            }
        }

        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected:" + bundle);
        m_shouldResolve = false;

        new AsyncTask<Void, Void, String>() {
            private String m_accountName;

            @Override
            protected String doInBackground(Void... params) {
                String scopes = "audience:server:client_id:" + SERVER_ID;

                m_accountName = Plus.AccountApi.getAccountName(m_googleApiClient);

                try {
                    return GoogleAuthUtil.getToken(getApplicationContext(), new Account(m_accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), scopes);
                } catch (IOException e) {
                    Log.e(TAG, "Error retrieving ID token.", e);
                } catch (GoogleAuthException e) {
                    Log.e(TAG, "Error retrieving ID token.", e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(String token) {
                Log.i(TAG, "ID token: " + token);

                if (token != null) {
                    Authorization authorization = new Authorization(Authorization.Type.GOOGLE, m_accountName, token);

                    getSettings().setAuthorization(authorization);

                    getConnection().enqueue(WebActivity.this, Session.createRegisterAction(WebActivity.this, authorization), new WebAction.ResultHandler<Session>() {
                        @Override
                        public void onActionResult(ee.ajapaik.android.data.util.Status status, Session session) {
                            if (session != null) {
                                m_settings.setSession(session);
                            }

                            invalidateAuthorization();
                        }
                    });
                } else {
                    // There was some error getting the ID Token
                    // ...
                }
            }

        }.execute(null, null, null);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!m_isResolving && m_shouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, PLAY_SERVICES_RESOLUTION_REQUEST);
                    m_isResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);

                    m_isResolving = false;
                    m_googleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an error dialog.
                //showErrorDialog(connectionResult);
            }
        } else {
            signOut();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FACEBOOK_SIGN_IN_RESOLUTION_REQUEST) {
            m_facebookCallback.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == GOOGLE_SIGN_IN_RESOLUTION_REQUEST) {
            if (resultCode != Activity.RESULT_OK) {
                m_shouldResolve = false;
            }

            m_isResolving = false;
            m_googleApiClient.connect();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public boolean isDialogFragmentVisible(int requestCode) {
        FragmentManager manager = getSupportFragmentManager();
        String tag = DIALOG_PREFIX + Integer.toString(requestCode);
        Fragment fragment = manager.findFragmentByTag(tag);

        return (fragment != null) ? true : false;
    }

    public void showDialogFragment(int requestCode) {
        showDialogFragment(requestCode, createDialogFragment(requestCode), null);
    }

    protected void showDialogFragment(int requestCode, DialogFragment dialog, Fragment fragment) {
        if (dialog != null) {
            String tag = DIALOG_PREFIX + Integer.toString(requestCode);
            FragmentManager manager = getSupportFragmentManager();

            if (fragment == null) {
                String rootTag = getDialogFragmentRootTag();

                if (rootTag != null) {
                    fragment = manager.findFragmentByTag(rootTag);
                } else {
                    Object[] fragments = getSupportFragmentManager().getFragments().toArray();

                    for (Object f : fragments) {
                        if (f instanceof WebFragment) {
                            fragment = (WebFragment) f;
                            break;
                        }
                    }
                }
            }

            if (fragment != null && manager.findFragmentByTag(tag) == null) {
                dialog.setTargetFragment(fragment, requestCode);
                dialog.show(manager, tag);
            }
        }
    }

    public void hideDialogFragment(int requestCode) {
        FragmentManager manager = getSupportFragmentManager();
        String tag = DIALOG_PREFIX + Integer.toString(requestCode);
        Fragment fragment = manager.findFragmentByTag(tag);

        if (fragment != null) {
            ((DialogFragment) fragment).dismiss();
        }
    }

    protected String getDialogFragmentRootTag() {
        return null;
    }
}
