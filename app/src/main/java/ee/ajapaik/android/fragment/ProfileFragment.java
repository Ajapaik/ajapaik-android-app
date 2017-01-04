package ee.ajapaik.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import ee.ajapaik.android.LoginActivity;
import ee.ajapaik.android.data.Hyperlink;
import ee.ajapaik.android.data.Profile;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.Authorization;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebAction;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ProfileFragment extends WebFragment {
    private static final String KEY_PROFILE = "profile";

    private Profile m_profile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Profile profile = null;

        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            profile = savedInstanceState.getParcelable(KEY_PROFILE);
        } else {
            getMainLayout().setVisibility(GONE);
        }

        getFacebookButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithFacebook();
            }
        });

        getGoogleButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        getLogoutButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        getUsernameLoginButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.start(getContext());
            }
        });

        setProfile(profile);
        invalidateLogin();
    }

    public void onAuthorizationChanged() {
        super.onAuthorizationChanged();

        invalidateLogin();
        onRefresh(false);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(KEY_PROFILE, m_profile);
    }

    @Override
    public void onStart() {
        super.onStart();
        onRefresh(false);
    }

    public Profile getProfile() {
        return m_profile;
    }

    public void setProfile(Profile profile) {
        if(!Objects.match(m_profile, profile)) {
            Context context = getActivity();
            View layout = getMainLayout();

            m_profile = profile;

            if(m_profile != null) {
                Hyperlink link = m_profile.getLink();
                String summary = context.getResources().getQuantityString(R.plurals.profile_rephotos, m_profile.getRephotosCount(), m_profile.getRephotosCount());

                if(m_profile.getRank() != 0) {
                    summary = summary + " " + context.getResources().getString(R.string.profile_rank, m_profile.getRank());
                }

                getNameView().setText((m_profile.getName() != null) ? m_profile.getName() : "");

                layout.setVisibility(VISIBLE);
                getTitleView().setText(Html.fromHtml(summary));

                if(m_profile.getMessage() != null) {
                    getSubtitleView().setText(m_profile.getMessage());
                }

                if(link != null) {
                    getLinkView().setText(link.toHtml());
                } else {
                    getLinkView().setVisibility(GONE);
                }
            } else {
                layout.setVisibility(GONE);
            }
        }
    }

    protected void onRefresh(final boolean animated) {
        Context context = getActivity();

        if(m_profile == null) {
            getProgressBar().setVisibility(VISIBLE);
        }

        getConnection().enqueue(context, Profile.createAction(context, (m_profile != null) ? m_profile : getSettings().getProfile()), new WebAction.ResultHandler<Profile>() {
            @Override
            public void onActionResult(Status status, Profile profile) {
                if(m_profile == null) {
                    getProgressBar().setVisibility(GONE);
                }

                if(profile != null) {
                    if(!Objects.match(m_profile, profile)) {
                        getSettings().setProfile(profile);
                    }

                    setProfile(profile);
                } else if(m_profile == null || animated) {
                    // TODO: Show error alert
                }
            }
        });
    }

    private void invalidateLogin() {
        Authorization authorization = getSettings().getAuthorization();
        toggleLoginButtons(isLoggedIn(authorization));
    }

    private boolean isLoggedIn(Authorization authorization) {
        return authorization != null && authorization.getType() != Authorization.Type.ANONYMOUS;
    }

    private void toggleLoginButtons(boolean isLoggedIn) {
        getLogoutButton().setVisibility(isLoggedIn ? VISIBLE : GONE);
        getFacebookButton().setVisibility(isLoggedIn ? GONE : VISIBLE);
        getGoogleButton().setVisibility(isLoggedIn ? GONE : VISIBLE);
        getLoginHintView().setVisibility(isLoggedIn ? GONE : VISIBLE);
        getUsernameLoginButton().setVisibility(isLoggedIn ? GONE : VISIBLE);
    }

    private View getMainLayout() {
        return getView().findViewById(R.id.layout_main);
    }

    private ProgressBar getProgressBar() {
        return (ProgressBar)getView().findViewById(R.id.progress_bar);
    }

    private TextView getNameView() {
        return (TextView)getView().findViewById(R.id.text_name);
    }

    private TextView getTitleView() {
        return (TextView)getView().findViewById(R.id.text_title);
    }

    private TextView getSubtitleView() {
        return (TextView)getView().findViewById(R.id.text_subtitle);
    }

    private TextView getLinkView() {
        return (TextView)getView().findViewById(R.id.text_link);
    }

    private LoginButton getFacebookButton() {
        return (LoginButton)getView().findViewById(R.id.button_action_facebook);
    }

    private SignInButton getGoogleButton() {
        return (SignInButton)getView().findViewById(R.id.button_action_google);
    }

    private Button getLogoutButton() {
        return (Button)getView().findViewById(R.id.button_action_logout);
    }
    private Button getUsernameLoginButton() {
        return (Button)getView().findViewById(R.id.button_action_login);
    }

    private TextView getLoginHintView() {
        return (TextView)getView().findViewById(R.id.login_hint);
    }
}
