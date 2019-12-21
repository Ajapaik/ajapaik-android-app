package ee.ajapaik.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import ee.ajapaik.android.LoginActivity;
import ee.ajapaik.android.R;
import ee.ajapaik.android.RegistrationActivity;
import ee.ajapaik.android.data.Hyperlink;
import ee.ajapaik.android.data.Profile;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.util.Authorization;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebAction;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static ee.ajapaik.android.ProfileActivity.RETURN_ACTIVITY;

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
                String returnActivity = getActivity().getIntent().getStringExtra(RETURN_ACTIVITY);
                LoginActivity.start(getContext(), returnActivity);
            }
        });

        getUsernameRegistrationButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String returnActivity = getActivity().getIntent().getStringExtra(RETURN_ACTIVITY);
                RegistrationActivity.start(getContext(), returnActivity);
            }
        });

        getSwipeRefreshLayout().setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                }
        );

        setProfile(profile);
        toggleLoginButtons();
    }

    public void onAuthorizationChanged() {
        super.onAuthorizationChanged();

        toggleLoginButtons();

        getSwipeRefreshLayout().setRefreshing(true);
        refresh();
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(KEY_PROFILE, m_profile);
    }

    @Override
    public void onStart() {
        super.onStart();

        getSwipeRefreshLayout().setRefreshing(true);
        Authorization authorization = getSettings().getAuthorization();

        if (authorization == null || authorization.isAnonymous()) {
            initUserInterface();
        } else {
            refresh();
        }
    }

    public Profile getProfile() {
        return m_profile;
    }

    public void setProfile(Profile profile) {
        if(!Objects.match(m_profile, profile)) {
            Context context = getActivity();

            m_profile = profile;

            if(m_profile != null) {
                if (!getSettings().getAuthorization().isAnonymous()) {
                    Hyperlink link = m_profile.getLink();
                    String summary = context.getResources().getQuantityString(R.plurals.profile_rephotos, m_profile.getRephotosCount(), m_profile.getRephotosCount());

                    getNameView().setText((m_profile.getName() != null) ? m_profile.getName() : "");

                    getTitleView().setText(Html.fromHtml(summary));

                    if (m_profile.getMessage() != null) {
                        getSubtitleView().setText(m_profile.getMessage());
                    }

                    if (link != null) {
                        getLinkView().setText(link.toHtml());
                    } else {
                        getLinkView().setVisibility(GONE);
                    }
                }
            }
        }
    }

    protected void refresh() {
        Context context = getActivity();

        getMainLayout().setVisibility(GONE);
        getConnection().enqueue(context, Profile.createAction(context, (m_profile != null) ? m_profile : getSettings().getProfile()), new WebAction.ResultHandler<Profile>() {
            @Override
            public void onActionResult(Status status, Profile profile) {
                if(profile != null) {
                    if(!Objects.match(m_profile, profile)) {
                        getSettings().setProfile(profile);
                    }

                    setProfile(profile);
                } else if(m_profile == null) {
                    showRequestErrorToast();
                }
                initUserInterface();
            }
        });
    }

    private void initUserInterface() {
        toggleLoginButtons();
        getMainLayout().setVisibility(VISIBLE);
        getSwipeRefreshLayout().setRefreshing(false);
    }

    private boolean isLoggedIn(Authorization authorization) {
        return authorization != null && authorization.getType() != Authorization.Type.ANONYMOUS;
    }

    private void toggleLoginButtons() {
        boolean isLoggedIn = isLoggedIn(getSettings().getAuthorization());
        getLogoutButton().setVisibility(isLoggedIn ? VISIBLE : GONE);
        getFacebookButton().setVisibility(isLoggedIn ? GONE : VISIBLE);
        getGoogleButton().setVisibility(isLoggedIn ? GONE : VISIBLE);
        getUsernameLoginButton().setVisibility(isLoggedIn ? GONE : VISIBLE);
        getUsernameRegistrationButton().setVisibility(isLoggedIn ? GONE : VISIBLE);
    }

    private View getMainLayout() {
        return getView().findViewById(R.id.layout_main);
    }

    private SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout)getView().findViewById(R.id.swiperefresh);
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

    private Button getFacebookButton() {
        return (Button)getView().findViewById(R.id.button_fb_login);
    }

    private Button getGoogleButton() {
        return (Button) getView().findViewById(R.id.button_google_login);
    }

    private Button getLogoutButton() {
        return (Button)getView().findViewById(R.id.button_action_logout);
    }

    private Button getUsernameLoginButton() {
        return (Button)getView().findViewById(R.id.button_action_login);
    }

    private Button getUsernameRegistrationButton() {
        return (Button)getView().findViewById(R.id.button_action_register);
    }

}
