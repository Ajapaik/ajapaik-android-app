package ee.ajapaik.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import ee.ajapaik.android.R;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.util.SearchService;
import ee.ajapaik.android.util.WebAction;

public abstract class SearchFragment extends WebFragment {

    protected abstract void performAction(Context context, WebAction action);
    protected abstract WebAction createSearchAction(String query);
    protected abstract void refresh();

    private boolean isSearchVisible;
    private SearchService m_searchService;
    private boolean m_isSearchResultVisible;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        m_searchService = new SearchService(new SearchService.Search() {
            @Override
            public void search(String query) {
                SearchFragment.this.search(query);
            }

            @Override
            public void clearSearch() {
                m_isSearchResultVisible = false;
                getSwipeRefreshLayout().setRefreshing(true);
                refresh();
            }
        });
    }

    public void search() {
        search(m_searchService.getQuery());
    }

    private void search(String query) {
        getSwipeRefreshLayout().setRefreshing(true);
        performAction(getActivity(), createSearchAction(query));
        m_isSearchResultVisible = true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isSearchVisible) m_searchService.initializeSearch(menu, inflater);
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout) getView().findViewById(R.id.swiperefresh);
    }

    void handleLoadingFinished() {
        getSwipeRefreshLayout().setRefreshing(false);
        if (!isSearchVisible) {
            isSearchVisible = true;
            getActivity().invalidateOptionsMenu();
        }
    }

    public boolean isSearchResultVisible() {
        return m_isSearchResultVisible;
    }

    protected void setSwipeRefreshListener() {
        getSwipeRefreshLayout().setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (isSearchResultVisible()) {
                            search();
                        } else {
                            refresh();
                        }
                    }
                }
        );
    }
}
