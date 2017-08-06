package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import ee.ajapaik.android.fragment.RegistrationFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.WebActivity;

public class RegistrationActivity extends WebActivity {
    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, String returnActivity) {
        Intent intent = new Intent(context, RegistrationActivity.class);
        if (returnActivity != null) {
            intent.putExtra(ProfileActivity.RETURN_ACTIVITY, returnActivity);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new RegistrationFragment()).commit();
        }
    }
}
