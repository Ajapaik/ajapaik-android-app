package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;

import ee.ajapaik.android.fragment.RephotosFragment;

public class RephotosActivity extends AlbumActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, RephotosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public RephotosFragment createFragment() {
        return new RephotosFragment();
    }
}
