package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import ee.ajapaik.android.fragment.ProfileFragment;
import ee.ajapaik.android.R;

public class ProfileActivity extends NavigationDrawerActivity {

    public static String RETURN_ACTIVITY = "lastActivity";

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, String returnActivity) {
        Intent intent = new Intent(context, ProfileActivity.class);
        if (returnActivity != null) {
            intent.putExtra(RETURN_ACTIVITY, returnActivity);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        configureNavigationDrawer();
        configureToolbar();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new ProfileFragment()).commit();
        }
    }
}
