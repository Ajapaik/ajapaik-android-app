package ee.ajapaik.android;

import android.os.Bundle;

import ee.ajapaik.android.fragment.LoginFragment;
import ee.ajapaik.android.util.WebActivity;

import ee.ajapaik.android.test.R;

public class LoginActivity extends WebActivity {
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
