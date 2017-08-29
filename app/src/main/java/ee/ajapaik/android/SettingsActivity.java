package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import ee.ajapaik.android.test.R;

public class SettingsActivity extends NavigationDrawerActivity {

    private static final String TAG_FRAGMENT = "fragment";

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        configureNavigationDrawer();
        configureToolbar();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new SettingsFragment(), TAG_FRAGMENT).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        public SettingsFragment() { }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName("defaultPreferences");
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
