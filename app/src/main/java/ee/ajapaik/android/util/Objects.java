package ee.ajapaik.android.util;

public class Objects {
    public static boolean match(Object a, Object b) {
        if(a == b) {
            return true;
        }

        if(a == null || b == null) {
            return false;
        }

        return a.equals(b);
    }
}
