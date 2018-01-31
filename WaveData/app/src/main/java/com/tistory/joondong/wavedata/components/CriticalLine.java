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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tistory.joondong.wavedata.parser.CommonParser;

/**
 * Created by isp40 on 2017-10-04.
 */

public class CriticalLine extends View {
    public interface InternalCLValueListener {
        public void getCLValue(float value);
    }
    public InternalCLValueListener mInternalCLValueListener;
    public void setInternalCLValueListener(CriticalLine.InternalCLValueListener listener) {
        mInternalCLValueListener = listener;
    }

    public interface ExternalCLValueListener {
        public void getCLValue(float value);
    }
    public ExternalCLValueListener mExternalCLValueListener;
    public void setExternalCLValueListener(CriticalLine.ExternalCLValueListener listener) {
        mExternalCLValueListener = listener;
    }

    private final static String CriticalLineManager = "[CriticalLine Manager] ";
    private final static String RecentCLValuePref = "RecentCLValuePref";

    private float mExtraRateWidth_hor;
    private float mExtraRateHeight_hor;
    private float mExtraRateWidth_ver;
    private float mExtraRateHeight_ver;
    private float mMaxValue;
    private float mLineWidthPixel;
    private boolean mDebugMode;

    private boolean mGotSize = false;
    private float mHeight;
    private float mWidth;
    private float mExtraHeight;
    private float mExtraWidth;
    private float mUsedHeight;

    private float mMinValue;
    private Paint mCLPnt;

    SharedPreferences mRecentPref;
    SharedPreferences.Editor mRecentPref_Editor;

    private float mCLValue;
    private float mPixelToValue;
    private float mValueToPixel; // number of pixel per 1 value.

    private boolean mIsValidTouch = false;
    private float mTouchInitialOffset;
    private float mTouchOffset;

    public CriticalLine(Context context) {
        super(context);
        mRecentPref = context.getSharedPreferences(RecentCLValuePref, 0);
        mRecentPref_Editor = mRecentPref.edit();
    }

    public CriticalLine(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mRecentPref = context.getSharedPreferences(RecentCLValuePref, 0);
        mRecentPref_Editor = mRecentPref.edit();
    }

    public void init(// Common settings
                     float extraRateHeight_hor,
                     float extraRateWidth_hor, // The ratio EdgeLine occupy when the orientation is horizontal mode.
                     float extraRateHeight_ver,
                     float extraRateWidth_ver, // The ratio EdgeLine occupy when the orientation is vertical mode.
                     float maxValue, // Max data size received frome peer. It is used at CriticalLine and WaveBars.
                     int lineWidth, // Edge line width(DP). it is used at EdgeLine and WaveBars.
                     boolean debugMode,

                     // Individual Settings
                     int criColor, // Critical line color
                     int criWidth, // Critical line width(DP)
                     float criMinValue) // Minimum value about maxValue that critical line can down.)
    {
        mExtraRateWidth_hor = extraRateWidth_hor;
        mExtraRateHeight_hor = extraRateHeight_hor;
        mExtraRateWidth_ver = extraRateWidth_ver;
        mExtraRateHeight_ver = extraRateHeight_ver;
        mMaxValue = maxValue;
        mLineWidthPixel = CommonParser.dpToPx(getContext(), lineWidth);
        mDebugMode = debugMode;

        mMinValue = criMinValue;
        mCLPnt = new Paint();
        mCLPnt.setColor(criColor);
        mCLPnt.setStrokeWidth(CommonParser.dpToPx(getContext(), criWidth));
    }

    protected void onDraw(Canvas canvas) {
        // get view size
        if(!mGotSize) {
            mHeight = canvas.getHeight();
            mWidth = canvas.getWidth();
            // If the orientation is vertical,
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                mExtraHeight = mHeight / mExtraRateHeight_ver;
                mExtraWidth = mWidth / mExtraRateWidth_ver;
                // If the orientation is horizontal,
            } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                mExtraHeight = mHeight / mExtraRateHeight_hor;
                mExtraWidth = mWidth / mExtraRateWidth_hor;
            }
            mUsedHeight = mHeight - mExtraHeight;

            float recentCLValue = mRecentPref.getFloat("RecentCLValue", -1);
            if(recentCLValue == -1) {
                mCLValue = mMaxValue * 3/4;
            } else {
                mCLValue = recentCLValue;
            }

            mValueToPixel = mUsedHeight / mMaxValue;
            mPixelToValue = mMaxValue / mUsedHeight;

            mInternalCLValueListener.getCLValue(mCLValue);
            if(mExternalCLValueListener != null)
                mExternalCLValueListener.getCLValue(mCLValue);

            mGotSize = true;
        }

        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawLine(mExtraWidth + mLineWidthPixel/2, mHeight - mCLValue * mValueToPixel, mWidth, mHeight - mCLValue * mValueToPixel, mCLPnt);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float getY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(mHeight - mCLValue * mValueToPixel - 45 < getY && getY < mHeight - mCLValue * mValueToPixel + 45) {
                    mIsValidTouch = true;
                    mTouchInitialOffset = getY;
                } else {
                    mIsValidTouch = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mIsValidTouch) {
                    if((getY - mTouchInitialOffset > 5) || getY - mTouchInitialOffset < -5) {
                        mTouchOffset = mTouchInitialOffset;
                        mTouchInitialOffset = getY;
                        setCLValue(mTouchOffset - mTouchInitialOffset);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mIsValidTouch = false;
                break;
        }
        return true;
    }

    /*
     * mCLValue is always modified by this method.
     *  */
    private void setCLValue(float dy) {
        mCLValue += dy * mPixelToValue;
        if(mCLValue > mMaxValue)
            mCLValue = mMaxValue;
        else if(mCLValue < mMinValue)
            mCLValue = mMinValue;
        mRecentPref_Editor.putFloat("RecentCLValue", mCLValue);
        mRecentPref_Editor.commit();
        mInternalCLValueListener.getCLValue(mCLValue);
        if(mExternalCLValueListener != null)
            mExternalCLValueListener.getCLValue(mCLValue);

        invalidate();
    }
}

