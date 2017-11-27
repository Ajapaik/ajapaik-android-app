package ee.ajapaik.android.util;

import android.content.Context;
import android.location.Location;
import android.text.format.DateFormat;

import java.util.Date;

import ee.ajapaik.android.R;

public class Strings {
    public static String toBase16(byte b) {
        return new String( new char[] { (char)fromBase16((b >> 4) & 0x0F), (char)fromBase16((b >> 0) & 0x0F) });
    }

    private static int fromBase16(int value) {
        if(value >= 0 && value <= 9) {
            return '0' + value;
        } else if(value >= 10 && value <= 15) {
            return 'A' + value - 10;
        }

        return '0';
    }

    public static String toLocalizedDate(Context context, Date date) {
        return (date != null) ? DateFormat.format("MMMM dd, yyyy", date).toString() : null;
    }

    public static String toLocalizedDistance(Context context, float distanceInMeters) {
        return (distanceInMeters < 1000) ?
                String.format("%d %s", (int)Math.round(distanceInMeters), context.getString(R.string.unit_m)) :
                String.format("%.1f %s", (distanceInMeters / 1000.0F), context.getString(R.string.unit_km));
    }

    public static String toLocalizedDistance(Context context, Location a, Location b) {
        return (a != null && b != null) ? toLocalizedDistance(context, a.distanceTo(b)) : "";
    }
}
