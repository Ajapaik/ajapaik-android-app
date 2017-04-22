package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import ee.ajapaik.android.fragment.ProfileFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.WebActivity;

public class ProfileActivity extends WebActivity {

    public static String LAST_ACTIVITY = "lastActivity";

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, String lastActivity) {
        Intent intent = new Intent(context, ProfileActivity.class);
        if (lastActivity != null) {
            intent.putExtra(LAST_ACTIVITY, lastActivity);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new ProfileFragment()).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && isFromLoginActivity()) {
            this.startActivity(new Intent(this, NearestActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isFromLoginActivity() {
        return "login".equals(getIntent().getStringExtra(LAST_ACTIVITY));
    }
}
