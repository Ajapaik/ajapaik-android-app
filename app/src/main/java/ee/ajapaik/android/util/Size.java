package ee.ajapaik.android.util;

public class Size {
    public final int width;
    public final int height;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean empty() {
        return (width <= 0 && height <= 0) ? true : false;
    }

    @Override
    public boolean equals(Object obj) {
        Size size = (Size)obj;

        if(size == this) {
            return true;
        }

        if(size == null || size.width != width || size.height != height) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "(" + Integer.toString(width) + ", " + Integer.toString(height) + ")";
    }
}
