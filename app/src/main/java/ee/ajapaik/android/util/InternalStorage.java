package ee.ajapaik.android.util;

import android.content.Context;

import java.io.File;

public class InternalStorage {

    public static void deleteFile(Context context, String filename) {
        new File(context.getFilesDir().getAbsolutePath() + "/" + filename).delete();
    }
}
