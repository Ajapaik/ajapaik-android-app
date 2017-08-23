package ee.ajapaik.android.util;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import ee.ajapaik.android.ProfileActivity;
import ee.ajapaik.android.WebService;
import ee.ajapaik.android.data.Profile;
import ee.ajapaik.android.data.Session;
import ee.ajapaik.android.fragment.util.DialogInterface;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.test.R;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static ee.ajapaik.android.ProfileActivity.RETURN_ACTIVITY;
import static ee.ajapaik.android.util.Authorization.Type.FACEBOOK;
import static ee.ajapaik.android.util.Authorization.Type.GOOGLE;

public class WebActivity extends AppCompatActivity implements DialogInterface, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "WebActivity";

    private static final String DIALOG_PREFIX = "dialog_";

    private WebService.Connection m_connection = new WebService.Connection();
    private Settings m_settings;

    public static final int FACEBOOK_SIGN_IN_RESOLUTION_REQUEST = 9001;
    private static final int GOOGLE_SIGN_IN_RESOLUTION_REQUEST = 9002;
    private static final int GET_ACCOUNTS_PERMISSION = 6001;

    private static final String SERVER_ID = "";
    private CallbackManager m_facebookCallback = null;
    private GoogleApiClient m_googleApiClient = null;

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
                        showProgressDialog("Logging in...");
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
    }

    private void showProgressDialog(String title) {
        progressDialog = ProgressDialog.show(WebActivity.this, title, "Please wait...");
    }

    public void signInWithUsername() {
        Authorization authorization = getSettings().getAuthorization();
        getConnection().enqueue(WebActivity.this, Session.createLoginAction(WebActivity.this, authorization), new WebAction.ResultHandler<Session>() {
            @Override
            public void onActionResult(ee.ajapaik.android.data.util.Status status, Session session) {
                if (session != null) {
                    login(session);
                } else {
                    getSettings().setAuthorization(null);
                    findViewById(R.id.login_unsuccessful).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.input_password)).setText("");
                }
            }
        });
    }

    public void registerWithUsername() {
        Authorization authorization = getSettings().getAuthorization();
        getConnection().enqueue(WebActivity.this, Session.createRegisterAction(WebActivity.this, authorization), new WebAction.ResultHandler<Session>() {
            @Override
            public void onActionResult(ee.ajapaik.android.data.util.Status status, Session session) {
                if (session != null) {
                    login(session);
                } else {
                    getSettings().setAuthorization(null);
                    findViewById(R.id.user_already_exists).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.input_password)).setText("");
                }
            }
        });
    }

    private void login(Session session) {
        dismissProgressDialog();
        m_settings.setSession(session);
        getSettings().setProfile(new Profile(getSettings().getSession().getAttributes()));
        String returnActivity = getIntent().getStringExtra(RETURN_ACTIVITY);
        finish();
        if (!"upload".equals(returnActivity)) {
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

    private void initGoogleAPI() {
        if (m_googleApiClient == null) {

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestServerAuthCode(SERVER_ID)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            m_googleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }
    }

    private void connectToGoogleApi() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(m_googleApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_RESOLUTION_REQUEST);
    }

    private void handleGoogleLoginResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            showProgressDialog("Logging in...");

            GoogleSignInAccount account = result.getSignInAccount();
            if (account == null) return;
            Authorization authorization = new Authorization(GOOGLE, account.getEmail(), account.getIdToken());
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
        showProgressDialog("Logging out...");
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
        if (m_googleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(m_googleApiClient);
        }
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
        initGoogleAPI();
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
    public void onConnected(Bundle bundle) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FACEBOOK_SIGN_IN_RESOLUTION_REQUEST) {
            m_facebookCallback.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == GOOGLE_SIGN_IN_RESOLUTION_REQUEST) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleLoginResult(result);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showDialogFragment(int requestCode) {
        DialogFragment dialog = createDialogFragment(requestCode);
        if (dialog != null) {
            String tag = DIALOG_PREFIX + Integer.toString(requestCode);
            FragmentManager manager = getSupportFragmentManager();

            Fragment fragment = null;

            Object[] fragments = getSupportFragmentManager().getFragments().toArray();

            for (Object f : fragments) {
                if (f instanceof WebFragment) {
                    fragment = (WebFragment) f;
                    break;
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
}
