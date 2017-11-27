package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import ee.ajapaik.android.fragment.LoginFragment;
import ee.ajapaik.android.R;
import ee.ajapaik.android.util.WebActivity;

public class LoginActivity extends WebActivity {
    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, String returnActivity) {
        Intent intent = new Intent(context, LoginActivity.class);
        if (returnActivity != null) {
            intent.putExtra(ProfileActivity.RETURN_ACTIVITY, returnActivity);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new LoginFragment()).commit();
        }
    }
}
