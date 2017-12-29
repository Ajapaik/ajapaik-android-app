package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsActivity extends NavigationDrawerActivity {

    private static final String TAG_FRAGMENT = "fragment";
    public static final String DEFAULT_PREFERENCES_KEY = "defaultPreferences";

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new SettingsFragment(), TAG_FRAGMENT).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        public SettingsFragment() { }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName(DEFAULT_PREFERENCES_KEY);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Preference myPref = findPreference("showTutorialPreference");
            myPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean showTutorialChecked = Boolean.valueOf(newValue.toString());
                    SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
                    editor.putBoolean("showTutorialWithoutAsking", showTutorialChecked);
                    editor.apply();
                    return true;
                }
            });
        }
    }
}
