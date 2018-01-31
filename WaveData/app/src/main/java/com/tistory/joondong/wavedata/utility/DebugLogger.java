package com.tistory.joondong.wavedata.utility;

import android.util.Log;

import com.tistory.joondong.wavedata.BuildConfig;

/**
 * Created by isp40 on 2017-10-06.
 */

public class DebugLogger {
    public static void v(final String tag, final String text) {
        if (BuildConfig.DEBUG)
            Log.v(tag, text);
    }

    public static void d(final String tag, final String text) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, text);
        }
    }

    public static void i(final String tag, final String text) {
        if (BuildConfig.DEBUG)
            Log.i(tag, text);
    }
}
