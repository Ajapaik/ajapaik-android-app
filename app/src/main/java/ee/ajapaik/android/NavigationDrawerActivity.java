package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.WebActivity;

public class NavigationDrawerActivity extends WebActivity {

    private DrawerLayout mDrawer;

    void configureToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,  mDrawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        mDrawer.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
    }

    void configureNavigationDrawer() {
        NavigationView navView = (NavigationView) findViewById(R.id.nvView);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Context context = NavigationDrawerActivity.this;
                switch (menuItem.getItemId()) {
                    case R.id.nearest:
                        finish();
                        context.startActivity(new Intent(context, NearestActivity.class));
                        break;
                    case R.id.albums:
                        finish();
                        AlbumsActivity.start(context);
                        break;
                    case R.id.profile:
                        ProfileActivity.start(context);
                        break;
                    case R.id.about:
                        AboutActivity.start(context);
                        break;
                }
                mDrawer.closeDrawer(GravityCompat.START, true);
                return false;
            }
        });
    }
}
