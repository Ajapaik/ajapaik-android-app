package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

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
                        if (context instanceof NearestActivity) break;
                        Intent intent = new Intent(context, NearestActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        finish();
                        break;
                    case R.id.albums:
                        if (context instanceof AlbumsActivity) break;
                        AlbumsActivity.start(context);
                        finish();
                        break;
                    case R.id.profile:
                        if (context instanceof ProfileActivity) break;
                        ProfileActivity.start(context);
                        break;
                    case R.id.local_rephotos:
                        if (context instanceof LocalRephotosActivity) break;
                        LocalRephotosActivity.start(context);
                        finish();
                        break;
                    case R.id.about:
                        if (context instanceof AboutActivity) break;
                        AboutActivity.start(context);
                        if (context instanceof ProfileActivity) finish();
                        break;
                    case R.id.settings:
                        if (context instanceof SettingsActivity) break;
                        SettingsActivity.start(context);
                        if (context instanceof ProfileActivity) finish();
                        break;
                }
                mDrawer.closeDrawer(GravityCompat.START, true);
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START, true);
        } else {
            super.onBackPressed();
        }
    }
}
