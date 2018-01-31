/*
 * Copyright (c) 2017, JoonDong
 * All rights reserved.
 * Copyright holder's blog : http://joondong.tistory.com
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Copyright holder's blog address must bo located under the copyright notice.
 *
 * 3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 4. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tistory.joondong.wavedata.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.tistory.joondong.wavedata.parser.CommonParser;

/**
 * Created by isp40 on 2017-10-04.
 */
public class Warning extends View implements WaveBars.WarningListener {

    private final static String WarningManager = "[Warning Manager] ";
    private final static String RecentVibability = "RecentVibability";

    private float mExtraRateWidth_hor;
    private float mExtraRateHeight_hor;
    private float mExtraRateWidth_ver;
    private float mExtraRateHeight_ver;
    private float mLineWidthPixel;

    private boolean mGotSize = false;
    private float mHeight;
    private float mWidth;
    private float mExtraHeight;
    private float mExtraWidth;
    //private float mUsedHeight;
    private float mUsedWidth;

    private int mWarnnginColor;
    public boolean mDoVibrate; // it is possible to be changed dinamically.
    private long mWarningTime;
    private int mTwinklingNumber;

    private Paint mWarningPnt;
    private boolean mReqLight;
    //private boolean mIsProgressing;
    private static boolean mIsProgressing;

    private Handler mWarningHandler;
    private Vibrator mVibrator;
    public long[] mVibrationPattern = new long[] {0, 100, 50, 100}; // it is possible to be changed dinamically.

    SharedPreferences mRecentPref;
    SharedPreferences.Editor mRecentPref_Editor;

    public Warning(Context context) {
        super(context);
        mVibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        mRecentPref = context.getSharedPreferences("RecentPref", 0);
        mRecentPref_Editor = mRecentPref.edit();
    }

    public Warning(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mVibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

        mRecentPref = context.getSharedPreferences("RecentPref", 0);
        mRecentPref_Editor = mRecentPref.edit();
    }

    public void init(// Common settings
                     float extraRateHeight_hor,
                     float extraRateWidth_hor, // The ratio EdgeLine occupy when the orientation is horizontal mode.
                     float extraRateHeight_ver,
                     float extraRateWidth_ver, // The ratio EdgeLine occupy when the orientation is vertical mode.
                     int lineWidth, // Edge line width(DP). it is used at EdgeLine and WaveBars.

                     // Individual settings
                     int warnColor, // Warning color
                     long warnTime, // Warning time
                     int twinkNum) // twinkling number
    {
        mExtraRateWidth_hor = extraRateWidth_hor;
        mExtraRateHeight_hor = extraRateHeight_hor;
        mExtraRateWidth_ver = extraRateWidth_ver;
        mExtraRateHeight_ver = extraRateHeight_ver;
        mLineWidthPixel = CommonParser.dpToPx(getContext(), lineWidth);

        mWarnnginColor = warnColor;

        boolean doVibrate = mRecentPref.getBoolean(RecentVibability, false);
        if(doVibrate == false)
            mDoVibrate = false;
        else
            mDoVibrate = true;

        mWarningTime = warnTime;
        mTwinklingNumber = twinkNum;

        mWarningHandler = new Handler();
    }

    protected void onDraw(Canvas canvas) {
        // get view size
        if(!mGotSize) {
            mHeight = canvas.getHeight();
            mWidth = canvas.getWidth();
            // If the orientation is vertical,
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                mExtraHeight = mHeight / mExtraRateHeight_ver;
                mExtraWidth = mWidth / mExtraRateWidth_ver;
                // If the orientation is horizontal,
            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mExtraHeight = mHeight / mExtraRateHeight_hor;
                mExtraWidth = mWidth / mExtraRateWidth_hor;
            }
            //mUsedHeight = mHeight - mExtraHeight;
            mUsedWidth = mWidth - mExtraWidth;

            mWarningPnt = new Paint();
            mWarningPnt.setShader(new RadialGradient(mExtraWidth + mUsedWidth/2, mHeight/2, mWidth>mHeight ? mWidth/2 : mHeight/2,
                    Color.TRANSPARENT, mWarnnginColor, Shader.TileMode.CLAMP));
            mWarningPnt.setAlpha(150);

            mGotSize = true;
        }

        if(mIsProgressing) {
            if(mReqLight) {
                canvas.drawRect(mExtraWidth + mLineWidthPixel/2, 0, mWidth, mHeight, mWarningPnt);
            } else {
                canvas.drawColor(Color.TRANSPARENT);
            }
        }
    }

    @Override
    public void startWarning() {
        if (!mIsProgressing) {
            Thread warningManager = new Thread(new Runnable() {
                @Override
                public void run() {
                    mIsProgressing = true;

                    if (mDoVibrate)
                        mVibrator.vibrate(mVibrationPattern, -1);
                    for (int i = 0; i < mTwinklingNumber * 2; i++) {
                        mWarningHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mReqLight = !mReqLight;
                                invalidate();
                            }
                        });
                        try {
                            Thread.sleep(mWarningTime / mTwinklingNumber / 2);
                        } catch (Exception e) {
                        }
                    }
                    mIsProgressing = false;
                }
            });
            warningManager.start();
        }
    }

    public void toggleVibrator() {
        mDoVibrate = !mDoVibrate;
        mRecentPref_Editor.putBoolean(RecentVibability, mDoVibrate);
        mRecentPref_Editor.commit();
    }

    public void setVibrator(boolean onOff) {
        mDoVibrate = onOff;
        mRecentPref_Editor.putBoolean(RecentVibability, onOff);
        mRecentPref_Editor.commit();
    }

    public boolean getVibrator() {
        boolean onOff = mRecentPref.getBoolean(RecentVibability, false);
        return onOff;
    }
}

