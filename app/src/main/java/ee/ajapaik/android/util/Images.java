package ee.ajapaik.android.util;

import ee.ajapaik.android.test.R;

public class Images {
    public static int toRephotoCountDrawableId(int rephotos) {
        if(rephotos >= 9) {
            return R.drawable.ic_filter_9_plus_white_36dp;
        } else if(rephotos == 8) {
            return R.drawable.ic_filter_8_white_36dp;
        } else if(rephotos == 7) {
            return R.drawable.ic_filter_7_white_36dp;
        } else if(rephotos == 6) {
            return R.drawable.ic_filter_6_white_36dp;
        } else if(rephotos == 5) {
            return R.drawable.ic_filter_5_white_36dp;
        } else if(rephotos == 4) {
            return R.drawable.ic_filter_4_white_36dp;
        } else if(rephotos == 3) {
            return R.drawable.ic_filter_3_white_36dp;
        } else if(rephotos == 2) {
            return R.drawable.ic_filter_2_white_36dp;
        } else if(rephotos == 1) {
            return R.drawable.ic_filter_1_white_36dp;
        }

        return R.drawable.transparent;
    }
}
