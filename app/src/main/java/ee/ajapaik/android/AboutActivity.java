package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

public class AboutActivity extends NavigationDrawerActivity implements View.OnClickListener {

    public static void start(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_about);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            ((TextView)findViewById(R.id.version_value)).setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.privacy_policy) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Ajapaik/Android-app/wiki/Ajapaik-Android-app-Privacy-Policy"));
            startActivity(intent);
        }
    }
}
