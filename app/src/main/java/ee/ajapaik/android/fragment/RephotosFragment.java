package ee.ajapaik.android.fragment;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import ee.ajapaik.android.ProfileActivity;
import ee.ajapaik.android.R;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.util.Authorization;
import ee.ajapaik.android.util.WebAction;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class RephotosFragment extends AlbumFragment {

    public static final String RETURN_ACTIVITY_NAME = "my-rephotos";

    @Override
    protected WebAction<Album> createAction(Context context) {
        return Album.createMyRephotosAction(context, getSettings().getLocation());
    }

    @Override
    protected WebAction createSearchAction(String query) {
        return Album.createRephotoSearchAction(getActivity(), query);
    }

    @Override
    protected String getPlaceholderString() {
        return getString(R.string.no_rephotos);
    }

    @Override
    public String getAlbumTitle() {
        return getString(R.string.my_rephotos);
    }

    @Override
    protected void refresh() {
        getSwipeRefreshLayout().setRefreshing(true);
        if (isLoggedIn()) {
            getNotLoggedInButton().setVisibility(GONE);
            super.refresh();
        } else {
            getNotLoggedInButton().setVisibility(VISIBLE);
            getNotLoggedInButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProfileActivity.start(getActivity(), RETURN_ACTIVITY_NAME);
                }
            });
            getSwipeRefreshLayout().setRefreshing(false);
            getEmptyView().setText(R.string.rephotos_label_not_logged_in_text);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private boolean isLoggedIn() {
        Authorization authorization = getSettings().getAuthorization();
        return authorization != null && !authorization.isAnonymous();
    }

    private Button getNotLoggedInButton() {
        return (Button) getView().findViewById(R.id.favorites_not_logged_in_action);
    }
}
