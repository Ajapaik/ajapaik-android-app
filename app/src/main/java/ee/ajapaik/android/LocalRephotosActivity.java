package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;

public class LocalRephotosActivity extends NavigationDrawerActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, LocalRephotosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}