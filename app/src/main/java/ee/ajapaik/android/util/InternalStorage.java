package ee.ajapaik.android.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class InternalStorage {

    private static final String TAG = "InternalStorage";

    public static void deleteFile(Context context, String filename) {
        new File(context.getFilesDir().getAbsolutePath() + "/" + filename).delete();
    }

    public static void saveFile(Context context, String filename, byte[] bytes) {
        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save file " + filename, e);
        }
    }
}
