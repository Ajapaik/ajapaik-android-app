package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;

import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.fragment.AlbumFragment;
import ee.ajapaik.android.fragment.PhotosFragment;
import ee.ajapaik.android.util.Search;

public class AlbumActivity extends NavigationDrawerActivity {
    private static final String TAG_FRAGMENT = "fragment";

    private static final String EXTRA_ALBUM_ID = "album_id";
    private static final String EXTRA_TITLE = "title";

    private Search m_search;

    public static Intent getStartIntent(Context context, String albumId, String title) {
        Intent intent = new Intent(context, AlbumActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra(EXTRA_ALBUM_ID, albumId);
        intent.putExtra(EXTRA_TITLE, title);

        return intent;
    }

    public static void start(Context context, Album album) {
        context.startActivity(getStartIntent(context, album.getIdentifier(), album.getTitle()));
    }

    public void setSearch(Search search) {
        m_search = search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album);
        configureNavigationDrawer();
        configureToolbar();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, createFragment(), TAG_FRAGMENT).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                m_search.search(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                return true;
            }
        });
        return true;
    }

    protected PhotosFragment createFragment() {
        AlbumFragment fragment = new AlbumFragment();

        fragment.setAlbumIdentifier(getIntent().getStringExtra(EXTRA_ALBUM_ID));

        return fragment;
    }

    protected PhotosFragment getFragment() {
        return (PhotosFragment)getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
    }
}
