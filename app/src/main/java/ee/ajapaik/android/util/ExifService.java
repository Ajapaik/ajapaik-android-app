package ee.ajapaik.android.util;

import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class ExifService {

    private static final String TAG = "ExifService";

    public static final String USER_COMMENT = "UserComment";

    public static String readField(File file, String tag) {
        try {
            return new ExifInterface(file.getAbsolutePath()).getAttribute(tag);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read upload data from image exif", e);
            return null;
        }
    }

    public static void writeField(File file, String tag, String value) {
        try {
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            exifInterface.setAttribute(tag, value);
            exifInterface.saveAttributes();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save value to exif! File=" + file.getName() +
                    ", tag=" + tag +
                    ", value=" + value, e);
        }
    }

    public static void deleteField(String filePath, String tag) {
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            exifInterface.setAttribute(tag, null);
            exifInterface.saveAttributes();
        } catch (Exception e) {
            Log.e(TAG, "Failed to set value to null in exif! File=" + filePath + ", tag=" + tag, e);
        }
    }
}
