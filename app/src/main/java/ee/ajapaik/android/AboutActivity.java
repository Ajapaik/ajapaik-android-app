package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import ee.ajapaik.android.test.R;

public class AboutActivity extends NavigationDrawerActivity implements View.OnClickListener {

    public static void start(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        configureNavigationDrawer();
        configureToolbar();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.privacy_policy) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ajapaik.ee"));
            startActivity(intent);
        }
    }
}
