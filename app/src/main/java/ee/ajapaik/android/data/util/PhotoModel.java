package ee.ajapaik.android.data.util;

import android.net.Uri;

public abstract class PhotoModel extends Model {

    protected Uri m_image;

    public Uri getThumbnail(int preferredDimension) {
        return resolve(m_image, preferredDimension);
    }

    public static Uri resolve(Uri uri, int preferredDimension) {
        if (uri != null) {
            String str = uri.toString();

            str = str.replace("[DIM]", Integer.toString(preferredDimension));

            return Uri.parse(str);
        }

        return uri;
    }
}
