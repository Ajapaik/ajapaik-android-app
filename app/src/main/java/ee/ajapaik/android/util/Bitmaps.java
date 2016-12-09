package ee.ajapaik.android.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class Bitmaps {
    public static final int FLIP_VERTICAL = 1;
    public static final int FLIP_HORIZONTAL = 2;

    public static Bitmap flip(Bitmap src, int type) {
        Matrix matrix = new Matrix();

        if(type == FLIP_VERTICAL) {
            matrix.preScale(1.0F, -1.0F);
        }  else if(type == FLIP_HORIZONTAL) {
            matrix.preScale(-1.0F, 1.0F);
        } else {
            return null;
        }

        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public static Bitmap rotate(Bitmap src, float angle) {
        Matrix matrix = new Matrix();

        matrix.preRotate(angle);

        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, false);
    }
}
