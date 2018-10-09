package ee.ajapaik.android.util;

import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;

import ee.ajapaik.android.R;

public class SearchService {

    private Search search;
    private SearchView m_searchView;

    public SearchService(Search search) {
        this.search = search;
    }

    public void initializeSearch(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        m_searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        m_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                m_searchView.clearFocus();
                search.search(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                return true;
            }
        });
        m_searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                search.clearSearch();
                return false;
            }
        });
    }

    public String getQuery() {
        return String.valueOf(m_searchView.getQuery());
    }

    public interface Search {
        void search(String query);
        void clearSearch();
    }
}
