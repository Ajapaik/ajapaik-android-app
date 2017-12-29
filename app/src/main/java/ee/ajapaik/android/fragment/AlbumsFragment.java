package ee.ajapaik.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import ee.ajapaik.android.R;
import ee.ajapaik.android.adapter.AlbumAdapter;
import ee.ajapaik.android.data.Feed;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.SearchService;
import ee.ajapaik.android.util.WebAction;

public class AlbumsFragment extends WebFragment {
    private static final String KEY_FEED = "feed";
    private static final String KEY_LIST = "list";

    private Feed m_feed;
    private Parcelable m_list;
    private SearchService m_searchService;

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
            Feed feed = savedInstanceState.getParcelable(KEY_FEED);

            setFeed(feed);

            m_list = savedInstanceState.getParcelable(KEY_LIST);

            if(m_list != null) {
                getListView().onRestoreInstanceState(m_list);
            }
        }

        m_searchService = new SearchService(new SearchService.Search() {
            @Override
            public void search(String query) {
                getSwipeRefreshLayout().setRefreshing(true);
                performAction(getActivity(), createSearchAction(query));
            }

            @Override
            public void clearSearch() {
                getSwipeRefreshLayout().setRefreshing(true);
                refresh();
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        m_searchService.initializeSearch(menu, inflater);
    }

    private WebAction<Feed> createSearchAction(String query) {
        return Feed.createSearchAction(getActivity(), query);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        m_list = getListView().onSaveInstanceState();

        savedInstanceState.putParcelable(KEY_FEED, m_feed);
        savedInstanceState.putParcelable(KEY_LIST, m_list);
    }

    public void setFeed(Feed feed) {
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
                getEmptyView().setText(R.string.albums_label_no_data);
                listView.setAdapter(null);
            }
        }
    }

    protected void refresh() {
        Context context = getActivity();
        WebAction<Feed> action = Feed.createAction(context);
        performAction(context, action);
    }

    private void performAction(Context context, WebAction<Feed> action) {
        getConnection().enqueue(context, action, new WebAction.ResultHandler<Feed>() {
            @Override
            public void onActionResult(Status status, Feed feed) {

                if(feed != null) {
                    setFeed(feed);
                } else if(m_feed == null) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                }
                getSwipeRefreshLayout().setRefreshing(false);
            }
        });
    }

    private TextView getEmptyView() {
        return (TextView)getView().findViewById(R.id.empty);
    }

    private ListView getListView() {
        return (ListView)getView().findViewById(R.id.list);
    }

    private SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout)getView().findViewById(R.id.swiperefresh);
    }
}
