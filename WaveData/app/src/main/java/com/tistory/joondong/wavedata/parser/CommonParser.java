package com.tistory.joondong.wavedata.parser;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by isp40 on 2017-10-04.
 */

public class CommonParser {
    // Source : http://dev.re.kr/14
    static public int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    // Source : http://dev.re.kr/14
    static public int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }
}
