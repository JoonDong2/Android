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

package com.tistory.joondong.wavedata;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.tistory.joondong.wavedata.components.CriticalLine;
import com.tistory.joondong.wavedata.components.EdgeLine;
import com.tistory.joondong.wavedata.components.Warning;
import com.tistory.joondong.wavedata.components.WaveBars;
import com.tistory.joondong.wavedata.packet.ArrayBundle;

/**
 * Created by isp40 on 2017-10-04.
 */

public class WaveDataView extends FrameLayout {
    private final static String WaveBarManager = "[WaveBar Manager] ";

    private boolean mIgnore = false;

    private static WaveDataView mWaveDataView;
    private CriticalLine mCriticalLine;
    private EdgeLine mEdgeLine;
    private WaveBars mWaveBars;
    private Warning mWarning;

    public WaveDataView(@NonNull Context context) {
        super(context);
        init(context);
    }
    public WaveDataView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout =inflater.inflate(R.layout.layout_wavedataview, this, false);
        addView(layout);
    }

    public static WaveDataView getView(Context context,
                                       // Common settings
                                       int backColor, // Background color. Actually it is used at WaveBars.
                                       float extraRateHeight_hor,
                                       float extraRateWidth_hor, // The ratio EdgeLine occupy when the orientation is horizontal mode.
                                       float extraRateHeight_ver,
                                       float extraRateWidth_ver, // The ratio EdgeLine occupy when the orientation is vertical mode.
                                       float maxValue, // Max data size received frome peer. It is used at CriticalLine and WaveBars.
                                       boolean debugMode,

                                       // EdgeLine settings
                                       int lineWidth, // Edge line width(DP). it is used at EdgeLine and WaveBars.
                                       int lineColor, // Edge line color
                                       String unit,
                                       float edgeMaxValue, // maxValue of common settings is standpoint.
                                       boolean isFraction,
                                       int numOfFraction,
                                       int edgeFontSize_ver, // Edge line font size(DP)
                                       int edgeFontSize_hor,
                                       int edgeFontColor, // Edge line font color

                                       // CriticalLne Settings
                                       int criColor, // Critical line color
                                       int criWidth, // Critical line width(DP)
                                       float criMinValue, // Minimum value about maxValue that critical line can down.

                                       // WaveBars settings
                                       int barColor,
                                       boolean gradient, // It start from Color.WHITE to barColor.
                                       int gradientColor,
                                       float intervalRate, // Bars's interval rate
                                       long period, // Rx period
                                       long oneFramePeriod, // One frame period of entire animation. the lower value the better quality.
                                       boolean autoAdjustPeriod,

                                       // Warning settings
                                       int warnColor, // Warning color
                                       long warnTime, // Warning time
                                       int twinkNum) // twinkling number
    {
        mWaveDataView = new WaveDataView(context);

        mWaveDataView.mCriticalLine = (CriticalLine)mWaveDataView.findViewById(R.id.criticalline);
        mWaveDataView.mCriticalLine.init(extraRateHeight_hor, extraRateWidth_hor, extraRateHeight_ver, extraRateWidth_ver, maxValue, lineWidth, debugMode, criColor, criWidth, criMinValue);

        mWaveDataView.mEdgeLine = (EdgeLine)mWaveDataView.findViewById(R.id.edgeline);
        mWaveDataView.mEdgeLine.init(extraRateHeight_hor, extraRateWidth_hor, extraRateHeight_ver, extraRateWidth_ver, lineWidth, debugMode, lineColor, unit, edgeMaxValue, isFraction, numOfFraction, edgeFontSize_ver, edgeFontSize_hor, edgeFontColor);

        mWaveDataView.mWaveBars = (WaveBars)mWaveDataView.findViewById(R.id.wavebars);
        mWaveDataView.mWaveBars.init(backColor, extraRateHeight_hor, extraRateWidth_hor, extraRateHeight_ver, extraRateWidth_ver, maxValue, lineWidth, debugMode, barColor, gradient, gradientColor, intervalRate, period, oneFramePeriod, autoAdjustPeriod);
        mWaveDataView.mCriticalLine.setInternalCLValueListener(mWaveDataView.mWaveBars);

        mWaveDataView.mWarning = (Warning)mWaveDataView.findViewById(R.id.warning);
        mWaveDataView.mWarning.init(extraRateHeight_hor, extraRateWidth_hor, extraRateHeight_ver, extraRateWidth_ver, lineWidth, warnColor, warnTime, twinkNum);
        mWaveDataView.mWaveBars.setWarningListener(mWaveDataView.mWarning);

        return mWaveDataView;
    }

    public boolean animateBars(float[] values) {
        if(!mIgnore) {
            ArrayBundle bundle = new ArrayBundle(values);
            mWaveBars.animateBars(bundle);
            return true;
        }
        return false;
    }

    public boolean animateBars(byte[] values) {
        if(!mIgnore) {
            ArrayBundle bundle = new ArrayBundle(values);
            mWaveBars.animateBars(bundle);
            return true;
        }
        return false;
    }

    public boolean animateBars(float[] values, float maxValue) {
        if(!mIgnore) {
            ArrayBundle bundle = new ArrayBundle(values, maxValue);
            mWaveBars.animateBars(bundle);
            return true;
        }
        return false;
    }

    public boolean toggleVibrator() {
        boolean previous = mWarning.getVibrator();
        mWarning.toggleVibrator();
        boolean current = mWarning.getVibrator();

        if(previous != current)
            return true;
        else
            return false;
    }

    public boolean setVibrator(boolean onOff) {
        mWarning.setVibrator(onOff);
        boolean current = mWarning.getVibrator();

        if(onOff == current)
            return true;
        else
            return false;
    }

    public boolean getVibrator() {
        return mWarning.getVibrator();
    }

    public void clearData() {
        mWaveBars.flushBundleList();
        mWaveBars.clearWaveBars();
    }

    public void ignoreReceive(boolean ignore) {
        mIgnore = ignore;
    }

    public float getCLValue() {
        return mWaveBars.getCLValue();
    }

    public void setCLValueListener(CriticalLine.ExternalCLValueListener listener) {
        mCriticalLine.setExternalCLValueListener(listener);
    }
}