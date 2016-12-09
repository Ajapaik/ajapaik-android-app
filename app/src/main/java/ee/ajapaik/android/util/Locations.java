package ee.ajapaik.android.util;

import android.content.Context;
import android.location.Location;

public class Locations {
    public static float getAngleInDegrees(Location a, Location b) {
        if(a != null && b != null) {
            double latitude1 = a.getLatitude();
            double longitude1 = a.getLongitude();
            double latitude2 = b.getLatitude();
            double longitude2 = b.getLongitude();
            double longitudeD = longitude2 - longitude1;
            double y = Math.sin(longitudeD) * Math.cos(latitude2);
            double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longitudeD);

            return (float)(((long)Math.toDegrees(Math.atan2(y, x)) + 360) % 360);
        }

        return 0.0F;
    }

    public static float getAzimuthInDegrees(Context context, Location a, Location b, float[] orientation) {
        float azimuth = getAngleInDegrees(a, b);

        if(orientation != null) {
            //azimuth += 360.0F * (0.0F - orientation[0]) / (2.0F * 3.14159F);
        }

        if(context != null) {
            /*switch(((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation()) {
                case Surface.ROTATION_0:
                    azimuth -= 0.0F;
                    break;
                case Surface.ROTATION_90:
                    azimuth -= 90.0F;
                    break;
                case Surface.ROTATION_180:
                    azimuth -= 180.0F;
                    break;
                default:
                    azimuth -= 270.0F;
                    break;
            }*/
        }

        azimuth %= 360.0F;

        return azimuth;
    }
}
