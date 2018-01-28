package ee.ajapaik.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;

import ee.ajapaik.android.R;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.util.SearchService;
import ee.ajapaik.android.util.WebAction;

public abstract class SearchFragment extends WebFragment {

    protected abstract void performAction(Context context, WebAction action);
    protected abstract WebAction createSearchAction(String query);
    protected abstract void refresh();

    private SearchService m_searchService;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        m_searchService.initializeSearch(menu, inflater);
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout)getView().findViewById(R.id.swiperefresh);
    }
}
