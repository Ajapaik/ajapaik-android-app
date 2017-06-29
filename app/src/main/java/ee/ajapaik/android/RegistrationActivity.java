package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import ee.ajapaik.android.fragment.RegistrationFragment;
import ee.ajapaik.android.test.R;

public class RegistrationActivity extends NavigationDrawerActivity {
    public static void start(Context context) {
        context.startActivity(new Intent(context, RegistrationActivity.class));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);
        configureNavigationDrawer();
        configureToolbar();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new RegistrationFragment()).commit();
        }
    }
}
