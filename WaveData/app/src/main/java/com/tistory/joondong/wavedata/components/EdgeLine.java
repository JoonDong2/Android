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
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.tistory.joondong.wavedata.parser.CommonParser;

/**
 * Created by isp40 on 2017-10-04.
 */

public class EdgeLine extends View {

    private final static String EdgeLineManager = "[Edge Line Manager] ";

    private float mExtraRateWidth_hor;
    private float mExtraRateHeight_hor;
    private float mExtraRateWidth_ver;
    private float mExtraRateHeight_ver;
    private boolean mDebugMode;

    private float mLineWidthPixel;
    private Paint mLinePnt;
    private Paint mFontPnt;
    private int mFontDp_ver;
    private int mFontDp_hor;
    private float mFontPixel;
    private String mUnit;

    private boolean mIsFraction = false;
    private float mMarkingValue1;
    private float mMarkingValue2;
    private float mMarkingValue3;
    private float mMarkingValue4;

    private float mHeight;
    private float mWidth;
    private float mExtraHeight;
    private float mExtraWidth;
    private float mUsedHeight;
    private float mMarkingLength;

    public EdgeLine(Context context) {
        super(context);
    }

    public EdgeLine(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(  // Common settings
                       float extraRateHeight_hor,
                       float extraRateWidth_hor,
                       float extraRateHeight_ver,
                       float extraRateWidth_ver,
                       int lineWidth, // Edge line width(DP)
                       boolean debugMode,

                       // Individual settings
                       int lineColor, // Edge line color
                       String unit,
                       float verMaxValue, // maxValue of common settings is standpoint.
                       boolean isFraction,
                       int numOfFraction,
                       int verFontSize_hor, // Edge line font size(DP) at the edge mode
                       int verFontSize_ver, // Edge line font size(DP) at the horizontal mode
                       int verFontColor) // Edge line font color
    {
        mExtraRateWidth_hor = extraRateWidth_hor;
        mExtraRateHeight_hor = extraRateHeight_hor;
        mExtraRateWidth_ver = extraRateWidth_ver;
        mExtraRateHeight_ver = extraRateHeight_ver;
        mDebugMode = debugMode;

        mLineWidthPixel = CommonParser.dpToPx(getContext(), lineWidth);
        mLinePnt = new Paint();
        mLinePnt.setStrokeWidth(mLineWidthPixel);
        mLinePnt.setColor(lineColor);

        if(unit == null)
            mUnit = "-";
        else
            mUnit = unit;

        mIsFraction = isFraction;

        // 10의 numOfFraction 제곱으로 수정.
        int square = (int)Math.pow(10, numOfFraction);
        mMarkingValue4 = (float)Math.round(verMaxValue * square) / square;
        mMarkingValue3 = (float)Math.round(verMaxValue * 3/4 * square) / square;
        mMarkingValue2 = (float)Math.round(verMaxValue * 1/2 * square) / square;
        mMarkingValue1 = (float)Math.round(verMaxValue * 1/4 * square) / square;

        mFontPnt = new Paint();
        mFontPnt.setColor(verFontColor);
        mFontPnt.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        mFontDp_ver = verFontSize_ver;
        mFontDp_hor = verFontSize_hor;
    }

    protected void onDraw(Canvas canvas) {
        // get view size
        mHeight = canvas.getHeight();
        mWidth = canvas.getWidth();
        // If the orientation is edge,
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mExtraHeight = mHeight / mExtraRateHeight_ver;
            mExtraWidth = mWidth / mExtraRateWidth_ver;
            mFontPixel = CommonParser.dpToPx(getContext(), mFontDp_ver);
            mFontPnt.setTextSize(mFontPixel);
            // If the orientation is horizontal,
        } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            mExtraHeight = mHeight / mExtraRateHeight_hor;
            mExtraWidth = mWidth / mExtraRateWidth_hor;
            mFontPixel = CommonParser.dpToPx(getContext(), mFontDp_hor);
            mFontPnt.setTextSize(mFontPixel);
        }
        mUsedHeight = mHeight - mExtraHeight;


        mMarkingLength = mExtraWidth / 4;
        float maxMarkingLength = CommonParser.dpToPx(getContext(), 20);
        if(mMarkingLength > maxMarkingLength)
            mMarkingLength = maxMarkingLength;

        // Edge line
        canvas.drawLine(mExtraWidth, 0, mExtraWidth, mHeight, mLinePnt);
        // Bottom line
        canvas.drawLine(mExtraWidth, mHeight - mLineWidthPixel/2, mWidth, mHeight - mLineWidthPixel/2, mLinePnt);
        // Top marking
        canvas.drawLine(mExtraWidth - mMarkingLength, mExtraHeight, mExtraWidth, mExtraHeight, mLinePnt);
        // 7/8 marking
        canvas.drawLine(mExtraWidth - mMarkingLength/2, mHeight - mUsedHeight*7/8, mExtraWidth, mHeight - mUsedHeight*7/8, mLinePnt);
        // 3/4 marking
        canvas.drawLine(mExtraWidth - mMarkingLength, mHeight - mUsedHeight*3/4, mExtraWidth, mHeight - mUsedHeight*3/4, mLinePnt);
        // 5/8 marking
        canvas.drawLine(mExtraWidth - mMarkingLength/2, mHeight - mUsedHeight*5/8, mExtraWidth, mHeight - mUsedHeight*5/8, mLinePnt);
        // 1/2 marking
        canvas.drawLine(mExtraWidth - mMarkingLength, mHeight - mUsedHeight*1/2, mExtraWidth, mHeight - mUsedHeight*1/2, mLinePnt);
        // 3/8 marking
        canvas.drawLine(mExtraWidth - mMarkingLength/2, mHeight - mUsedHeight*3/8, mExtraWidth, mHeight - mUsedHeight*3/8, mLinePnt);
        // 1/4 marking
        canvas.drawLine(mExtraWidth - mMarkingLength, mHeight - mUsedHeight*1/4, mExtraWidth, mHeight - mUsedHeight*1/4, mLinePnt);
        // 1/8 marking
        canvas.drawLine(mExtraWidth - mMarkingLength/2, mHeight - mUsedHeight*1/8, mExtraWidth, mHeight - mUsedHeight*1/8, mLinePnt);
        // Bottom marking
        canvas.drawLine(mExtraWidth - mMarkingLength, mHeight - mLineWidthPixel/2, mExtraWidth, mHeight - mLineWidthPixel/2, mLinePnt);

        float textMargin = CommonParser.dpToPx(getContext(), 10);
        if(mIsFraction) {
            canvas.drawText(((Float)mMarkingValue4).toString(), (mExtraWidth - mMarkingLength)/(((Float)mMarkingValue4).toString().length() + 1), mExtraHeight + mFontPixel /3, mFontPnt);
            canvas.drawText(((Float)mMarkingValue3).toString(), (mExtraWidth - mMarkingLength)/(((Float)mMarkingValue3).toString().length() + 1), mHeight - mUsedHeight*3/4 + mFontPixel /3, mFontPnt);
            canvas.drawText(((Float)mMarkingValue2).toString(), (mExtraWidth - mMarkingLength)/(((Float)mMarkingValue2).toString().length() + 1), mHeight - mUsedHeight*1/2 + mFontPixel /3, mFontPnt);
            canvas.drawText(((Float)mMarkingValue1).toString(), (mExtraWidth - mMarkingLength)/(((Float)mMarkingValue1).toString().length() + 1), mHeight - mUsedHeight*1/4 + mFontPixel /3, mFontPnt);
        } else {
            canvas.drawText(((Integer)((int)mMarkingValue4)).toString(), (mExtraWidth - mMarkingLength)/(((Integer)((int)mMarkingValue4)).toString().length() + 1), mExtraHeight + mFontPixel /3, mFontPnt);
            canvas.drawText(((Integer)((int)mMarkingValue3)).toString(), (mExtraWidth - mMarkingLength)/(((Integer)((int)mMarkingValue3)).toString().length() + 1), mHeight - mUsedHeight*3/4 + mFontPixel /3, mFontPnt);
            canvas.drawText(((Integer)((int)mMarkingValue2)).toString(), (mExtraWidth - mMarkingLength)/(((Integer)((int)mMarkingValue2)).toString().length() + 1), mHeight - mUsedHeight*1/2 + mFontPixel /3, mFontPnt);
            canvas.drawText(((Integer)((int)mMarkingValue1)).toString(), (mExtraWidth - mMarkingLength)/(((Integer)((int)mMarkingValue1)).toString().length() + 1), mHeight - mUsedHeight*1/4 + mFontPixel /3, mFontPnt);
        };
        canvas.drawText("0", (mExtraWidth - mMarkingLength)/3, mHeight, mFontPnt);

        if(mUnit.length() == 1)
            canvas.drawText("(" + mUnit + ")", (mExtraWidth - mMarkingLength)/2, (mExtraHeight + mFontPixel)/3, mFontPnt);
        else
            canvas.drawText("(" + mUnit + ")", (mExtraWidth - mMarkingLength)/(mUnit.length() + 1), (mExtraHeight + mFontPixel)/3, mFontPnt);
    }
}