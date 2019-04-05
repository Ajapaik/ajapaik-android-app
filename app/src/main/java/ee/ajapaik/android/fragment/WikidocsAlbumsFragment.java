package ee.ajapaik.android.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import ee.ajapaik.android.R;
import ee.ajapaik.android.adapter.AlbumAdapter;
import ee.ajapaik.android.data.WikidocsFeed;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebAction;

public class WikidocsAlbumsFragment extends SearchFragment {
    private static final String KEY_FEED = "feed";
    private static final String KEY_LIST = "list";

    private WikidocsFeed m_feed;
    private Parcelable m_list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_albums, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getSwipeRefreshLayout().setRefreshing(true);
        refresh();

        if(savedInstanceState != null) {
            WikidocsFeed feed = savedInstanceState.getParcelable(KEY_FEED);

            setFeed(feed);

            m_list = savedInstanceState.getParcelable(KEY_LIST);

            if(m_list != null) {
                getListView().onRestoreInstanceState(m_list);
            }
        }

        setSwipeRefreshListener();
    }

    @Override
    public WebAction<WikidocsFeed> createSearchAction(String query) {
        Location location = getSettings().getLocation();
        return WikidocsFeed.createSearchAction(getActivity(), query, location);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        m_list = getListView().onSaveInstanceState();

        savedInstanceState.putParcelable(KEY_FEED, m_feed);
        savedInstanceState.putParcelable(KEY_LIST, m_list);
    }

    public void setFeed(WikidocsFeed feed) {
        if(!Objects.match(m_feed, feed)) {
            ListView listView = getListView();

            m_feed = feed;

            if(m_feed != null) {
                View v = listView.getChildAt(0);
                int lastViewedPosition = listView.getFirstVisiblePosition();
                int topOffset = (v == null) ? 0 : v.getTop();

                listView.setAdapter(new AlbumAdapter(listView.getContext(), m_feed.getAlbums()));
                listView.setSelectionFromTop(lastViewedPosition, topOffset);
            } else {
                getEmptyView().setText(isSearchResultVisible() ? R.string.no_search_result : R.string.albums_label_no_data);
                listView.setAdapter(null);
            }
        }
    }

    @Override
    protected void refresh() {
        Context context = getActivity();
        Location location = getSettings().getLocation();
        WebAction<WikidocsFeed> action = WikidocsFeed.createAction(context, location);
        performAction(context, action);
    }

    @Override
    public void performAction(Context context, WebAction action) {
        getConnection().enqueue(context, action, new WebAction.ResultHandler<WikidocsFeed>() {
            @Override
            public void onActionResult(Status status, WikidocsFeed feed) {

                if(feed != null) {
                    setFeed(feed);
                } else if(m_feed == null) {
                    showRequestErrorToast();
                }
                handleLoadingFinished();
            }
        });
    }

    private TextView getEmptyView() {
        return (TextView)getView().findViewById(R.id.empty);
    }

    private ListView getListView() {
        return (ListView)getView().findViewById(R.id.list);
    }

}
