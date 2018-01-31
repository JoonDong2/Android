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
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import com.tistory.joondong.wavedata.packet.ArrayBundle;
import com.tistory.joondong.wavedata.parser.CommonParser;
import com.tistory.joondong.wavedata.utility.DebugLogger;

import java.util.LinkedList;

/**
 * Created by isp40 on 2017-10-04.
 */
public class WaveBars extends SurfaceView implements SurfaceHolder.Callback, CriticalLine.InternalCLValueListener, KeyEvent.Callback{
    private final static String WaveBarManager = "[WaveBar Manager] ";

    private boolean mAutoAdjustPeriod;
    private static int mAdjustLevel;

    // The Warning class listen this listener.
    public interface WarningListener {
        public void startWarning();
    }
    private WarningListener mWarningListener;
    public void setWarningListener(WarningListener listener) {
        mWarningListener = listener;
    }

    // This class listen the CriticalLine.CLValueListener to get mCLValue.
    private static float mCLValue;
    @Override
    public void getCLValue(float value) {
        mCLValue = value;
        DebugLogger.d(WaveBarManager, "WaveDataView::getCLValue : "+((Float)value).toString());
    }

    private int mBackgroundColor;
    private float mExtraRateWidth_hor;
    private float mExtraRateHeight_hor;
    private float mExtraRateWidth_ver;
    private float mExtraRateHeight_ver;
    private float mMaxValue;
    private float mLineWidthPixel;
    private boolean mDebugMode;

    private int mBarColor;
    private boolean mIsGradient;
    private int mGradientColor;
    private float mIntervalRate;
    private long mInitialPeriod;
    private static long mPeriod;
    private long mInitialNumOfFrame;
    private static long mNumOfFrame;

    private float mHeight;
    private float mWidth;
    private float mExtraHeight;
    private float mExtraWidth;
    private float mUsedHeight;
    private float mUsedWidth;

    SurfaceHolder mHolder;
    static private LinkedList<ArrayBundle> mBundleList = new LinkedList<>();
    private AnimateBarsThread mAnimateBars;

    public WaveBars(Context context) {
        super(context);

        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public WaveBars(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void init(   // Common settings
                        int backColor, // Background color. Actually it is used at WaveBars.
                        float extraRateHeight_hor,
                        float extraRateWidth_hor, // The ratio EdgeLine occupy when the orientation is horizontal mode.
                        float extraRateHeight_ver,
                        float extraRateWidth_ver, // The ratio EdgeLine occupy when the orientation is vertical mode.
                        float maxValue, // Max data size received frome peer. It is used at CriticalLine and WaveBars.
                        int lineWidth, // Edge line width(DP). it is used at EdgeLine and WaveBars.
                        boolean debugMode,

                        // Individual settings
                        int barColor,
                        boolean gradient, // It start from Color.WHITE to barColor.
                        int gradientColor,
                        float intervalRate, // Bars's interval rate
                        long period, // Rx period (20-bytes)
                        long oneFramePeriod, // One frame period of entire animation. the lower value the better quality.
                        boolean autoAdjustPeriod)
    {
        mBackgroundColor = backColor;
        mExtraRateWidth_hor = extraRateWidth_hor;
        mExtraRateHeight_hor = extraRateHeight_hor;
        mExtraRateWidth_ver = extraRateWidth_ver;
        mExtraRateHeight_ver = extraRateHeight_ver;
        mMaxValue = maxValue;
        mLineWidthPixel = CommonParser.dpToPx(getContext(), lineWidth);
        mDebugMode = debugMode;

        mBarColor = barColor;
        mIsGradient = gradient;
        mGradientColor = gradientColor;

        mIntervalRate = intervalRate;
        mInitialPeriod = period;
        if(mPeriod == 0)
            mPeriod = period;

        mInitialNumOfFrame = mPeriod / oneFramePeriod;
        if(mNumOfFrame == 0)
            mNumOfFrame = mInitialNumOfFrame;

        mAutoAdjustPeriod = autoAdjustPeriod;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // get view size
        Canvas canvas = holder.lockCanvas();
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
        mUsedWidth = mWidth - mExtraWidth;
        canvas.drawColor(mBackgroundColor);
        mHolder.unlockCanvasAndPost(canvas);

        mAnimateBars = new AnimateBarsThread(holder);
        mAnimateBars.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        for(;;) {
            try {
                mAnimateBars.join();
                break;
            } catch (Exception e) {}
        }
    }

    public void animateBars(ArrayBundle bundle) {
        mBundleList.add(bundle);

        if(mAutoAdjustPeriod == true){
            if(mBundleList.size() > 3) {
                if (mBundleList.size() < 6 && mAdjustLevel != 1) {
                    mAdjustLevel = 1;
                    mPeriod = (long) (mInitialPeriod * 0.9);
                    mNumOfFrame = (long) (mInitialNumOfFrame * 0.9);
                } else if (mBundleList.size() < 9 && mAdjustLevel != 2) {
                    mAdjustLevel = 2;
                    mPeriod = (long) (mInitialPeriod * 0.8);
                    mNumOfFrame = (long) (mInitialNumOfFrame * 0.8);
                } else if (mBundleList.size() < 12 && mAdjustLevel != 3) {
                    mAdjustLevel = 3;
                    mPeriod = (long) (mInitialPeriod * 0.7);
                    mNumOfFrame = (long) (mInitialNumOfFrame * 0.7);
                } else if (mAdjustLevel != 4) {
                    mAdjustLevel = 4;
                    mPeriod = (long) (mInitialPeriod * 0.6);
                    mNumOfFrame = (long) (mInitialNumOfFrame * 0.6);
                }
            } else {
                mAdjustLevel = 0;
                mPeriod = mInitialPeriod;
                mNumOfFrame = mInitialNumOfFrame;
            }
        }


        if(mAnimateBars != null) {
            if (mAnimateBars.isAlive() != true) {
                mAnimateBars = new AnimateBarsThread(mHolder);
                mAnimateBars.start();
            }
        }
    }

    public class AnimateBarsThread extends Thread {
        SurfaceHolder mHolder;

        AnimateBarsThread(SurfaceHolder holder) {
            mHolder = holder;
        }

        @Override
        public void run() {
            while(mBundleList.isEmpty() != true) {
                ArrayBundle bundle = mBundleList.pop();
                DebugLogger.d(WaveBarManager, "Size of BundleList : " + ((Integer)mBundleList.size()).toString());

                if(bundle.mMaxValue > mCLValue) {
                    mWarningListener.startWarning();
                    DebugLogger.d(WaveBarManager, "mMaxValue = " + ((Float)bundle.mMaxValue).toString() + " mCLValue = " + ((Float)mCLValue).toString());
                    DebugLogger.d(WaveBarManager, "Warning!!");
                }

                if(!animateFluct(mHolder, bundle.mValues, 2, (float)1.3))
                    return;
            }
        }
    }

    public boolean animateFluct(SurfaceHolder holder, float[] values, float upPeriodRatio, float limitRatio) {
        if(animateZeroToTop(holder, values, upPeriodRatio)) {
            float downPeriodRatio = upPeriodRatio/(upPeriodRatio-1);
            if(mBundleList.isEmpty()){
                if(!animateTopToZero(holder, values, downPeriodRatio))
                    return false;
            } else {
                if(!animateTopToLimit(holder, values, downPeriodRatio, limitRatio))
                    return false;
            }
        } else {
            return false;
        }
        return true;
    }

    /* @brief       Basic method for animating each value from zoro to the value.
     * param[in]    holder          SurfaceHolder to draw to surface in the AnimateBarsThread.
     * param[in]    periodRatio     Percentage of entire duration.
     */
    public boolean animateZeroToTop(SurfaceHolder holder, float[] mValues, float periodRatio) {
        long m_start = System.currentTimeMillis();
        long period = (long) (mPeriod / periodRatio);
        long numOfFrame = (long) (mNumOfFrame / periodRatio);

        int bundleLength = mValues.length;
        // Bar's rect standpoints
        float constantWidth = mUsedWidth / bundleLength; // Rect's standpoints of Left and Right.
        float constantBottom = mHeight - mLineWidthPixel; // For condisering bottom line width.
        float interval = mUsedWidth / bundleLength / mIntervalRate;
        float offset = mExtraWidth;

        Canvas canvas;
        Paint barPnt = new Paint();
        if(mIsGradient)
            barPnt.setShader(new LinearGradient(0, mExtraHeight, 0, mHeight, mBarColor,mGradientColor, Shader.TileMode.CLAMP));
        else
            barPnt.setColor(mBarColor);

        if (numOfFrame != 0) {
            float constantHeight = mUsedHeight / (mMaxValue * numOfFrame);
            for (float frame = 0; frame < numOfFrame + 1; frame++) {
                long start = System.currentTimeMillis();
                canvas = holder.lockCanvas();
                if (canvas == null)
                    return false;

                canvas.drawColor(mBackgroundColor);

                for (int bundleIndex = 1; bundleIndex < bundleLength + 1; bundleIndex++) {
                    if (mValues[bundleIndex - 1] > mMaxValue)
                        mValues[bundleIndex - 1] = mMaxValue;
                    //canvas.drawRect(offset + (bundleIndex - 1) * constantWidth + interval, mHeight - (mUsedHeight * (mValues[bundleIndex - 1] / mMaxValue)) * frame / numOfFrame - mLineWidthPixel, offset + bundleIndex * constantWidth, constantBottom, barPnt);
                    canvas.drawRect(offset + (bundleIndex - 1) * constantWidth + interval, mHeight - (mValues[bundleIndex - 1] * frame * constantHeight) - mLineWidthPixel, offset + bundleIndex * constantWidth, constantBottom, barPnt);
                }

                holder.unlockCanvasAndPost(canvas);

                long passedTime = System.currentTimeMillis() - start;
                if (passedTime < period / (numOfFrame + 1)) {
                    try {
                        Thread.sleep((long) (period / (numOfFrame + 1)) - passedTime);
                    } catch (Exception e) {
                    }
                }
            }
        } else if (numOfFrame == 0) {
            long start = System.currentTimeMillis();
            float constantHeight = mUsedHeight / mMaxValue;
            canvas = holder.lockCanvas();
            if (canvas == null)
                return false;

            canvas.drawColor(mBackgroundColor);

            for (int bundleIndex = 1; bundleIndex < bundleLength + 1; bundleIndex++) {
                if (mValues[bundleIndex - 1] > mMaxValue)
                    mValues[bundleIndex - 1] = mMaxValue;
                canvas.drawRect(offset + (bundleIndex - 1) * constantWidth + interval, mHeight - (constantHeight * mValues[bundleIndex - 1]) - mLineWidthPixel, offset + bundleIndex * constantWidth, constantBottom, barPnt);
            }
            holder.unlockCanvasAndPost(canvas);

            long passedTime = System.currentTimeMillis() - start;
            if(passedTime < period/(numOfFrame +1)) {
                try {
                    Thread.sleep((long) (period / (numOfFrame + 1)) - passedTime);
                } catch (Exception e) {
                }
            }
        }
        Long m_passedTIme = System.currentTimeMillis() - m_start;
        if(m_passedTIme > (long)(period * 1.1))
            DebugLogger.d(WaveBarManager, "[method : animateZeroToTop]passed time is "+ m_passedTIme.toString() + "ms" + " period is " + ((Long)mPeriod).toString() + "ms" + "\nperiod is too short. extra time is nedded.");
        else
            DebugLogger.d(WaveBarManager, "[method : animateZeroToTop]passed time is "+ m_passedTIme.toString() + "ms" + " period is " + ((Long)mPeriod).toString() + "ms");
        return true;
    }

    /* @brief       Basic method for animating each value from the value to limitation.
     *
     * param[in]    holder          SurfaceHolder to draw to surface in the AnimateBarsThread.
     * param[in]    periodRatio     Percentage of entire duration.
     * param[in]    limitRatio      The bars down to each value / this ratio.
     */
    public boolean animateTopToLimit(SurfaceHolder holder, float[] mValues, float periodRatio,float limitRatio) {
        long m_start = System.currentTimeMillis();
        long period = (long)(mPeriod / periodRatio);
        long numOfFrame = (long)(mNumOfFrame / periodRatio);

        int bundleLength = mValues.length;
        // Bar's rect standpoints
        float constantWidth = mUsedWidth/bundleLength; // Rect's standpoints of Left and Right.
        float constantBottom = mHeight - mLineWidthPixel; // For condisering bottom line width.
        float interval = mUsedWidth/bundleLength/mIntervalRate;
        float offset = mExtraWidth;

        float constantlimitRatio = 1/limitRatio;
        float constantExtraHeight = (1-constantlimitRatio) / numOfFrame;

        Canvas canvas;
        Paint barPnt = new Paint();
        if(mIsGradient)
            barPnt.setShader(new LinearGradient(0, mExtraHeight, 0, mHeight, mBarColor,mGradientColor, Shader.TileMode.CLAMP));
        else
            barPnt.setColor(mBarColor);

        if(numOfFrame != 0) {
            float constantHeight = mUsedHeight / mMaxValue;
            for(float frame = numOfFrame; frame >= 0; frame--) {
                long start = System.currentTimeMillis();
                canvas = holder.lockCanvas();
                if(canvas == null)
                    return false;

                canvas.drawColor(mBackgroundColor);

                for(int bundleIndex = 1; bundleIndex < bundleLength + 1; bundleIndex++) {
                    if(mValues[bundleIndex-1] > mMaxValue)
                        mValues[bundleIndex-1] = mMaxValue;
                    //canvas.drawRect(offset + (bundleIndex-1)*constantWidth + interval, mHeight - (mUsedHeight*(mValues[bundleIndex-1]/mMaxValue))*(constantlimitRatio + (1-constantlimitRatio)*(frame/numOfFrame))  - mLineWidthPixel, offset + bundleIndex*constantWidth, constantBottom, barPnt);
                    canvas.drawRect(offset + (bundleIndex-1)*constantWidth + interval, mHeight - (constantHeight*(mValues[bundleIndex-1]*(constantlimitRatio + constantExtraHeight * frame)))  - mLineWidthPixel, offset + bundleIndex*constantWidth, constantBottom, barPnt);
                }

                holder.unlockCanvasAndPost(canvas);

                long passedTime = System.currentTimeMillis() - start;
                if(passedTime < period/(numOfFrame +1)) {
                    try {
                        Thread.sleep((long)(period/(numOfFrame +1))-passedTime);
                    } catch (Exception e) {
                    }
                }
            }
        } else if (numOfFrame == 0) {
            long start = System.currentTimeMillis();
            float constantHeight = mUsedHeight / mMaxValue * constantlimitRatio;
            canvas = holder.lockCanvas();
            if(canvas == null)
                return false;

            canvas.drawColor(mBackgroundColor);

            for(int bundleIndex = 1; bundleIndex < bundleLength + 1; bundleIndex++) {
                if(mValues[bundleIndex-1] > mMaxValue)
                    mValues[bundleIndex-1] = mMaxValue;
                canvas.drawRect(offset + (bundleIndex-1)*constantWidth + interval, mHeight - (constantHeight * mValues[bundleIndex-1]) - mLineWidthPixel, offset + bundleIndex*constantWidth, constantBottom, barPnt);
            }

            holder.unlockCanvasAndPost(canvas);

            long passedTime = System.currentTimeMillis() - start;
            if(passedTime < period/(numOfFrame +1)) {
                try {
                    Thread.sleep((long)(period/(numOfFrame +1))-passedTime);
                } catch (Exception e) {
                }
            }
        }

        Long m_passedTIme = System.currentTimeMillis() - m_start;
        if(m_passedTIme > (long)(period * 1.1))
            DebugLogger.d(WaveBarManager, "[method : animateTopToLimit]passed time is "+ m_passedTIme.toString() + "ms" + " period is " + ((Long)mPeriod).toString() + "ms" + "\nperiod is too short. extra time is nedded.");
        else
            DebugLogger.d(WaveBarManager, "[method : animateTopToLimit]passed time is "+ m_passedTIme.toString() + "ms" + " period is " + ((Long)mPeriod).toString() + "ms");
        return true;
    }

    /* @brief       Basic method for animating each bar from the height corresponding the value to zero.
     * param[in]    holder          SurfaceHolder to draw to surface in the AnimateBarsThread.
     * param[in]    periodRatio     Percentage of entire duration.
     */
    public boolean animateTopToZero(SurfaceHolder holder, float[] mValues, float periodRatio) {
        long m_start = System.currentTimeMillis();
        long period = (long)(mPeriod / periodRatio);
        long numOfFrame = (long)(mNumOfFrame / periodRatio);

        int bundleLength = mValues.length;
        // Bar's rect standpoints
        float constantWidth = mUsedWidth/bundleLength; // Rect's standpoints of Left and Right.
        float constantBottom = mHeight - mLineWidthPixel; // For condisering bottom line width.
        float interval = mUsedWidth/bundleLength/mIntervalRate;
        float offset = mExtraWidth;

        Canvas canvas;
        Paint barPnt = new Paint();
        if(mIsGradient)
            barPnt.setShader(new LinearGradient(0, mExtraHeight, 0, mHeight, mBarColor,mGradientColor, Shader.TileMode.CLAMP));
        else
            barPnt.setColor(mBarColor);

        if(numOfFrame != 0) {
            float constantHeight = mUsedHeight / (mMaxValue * numOfFrame);
            for(float frame = numOfFrame; frame >= 0; frame--) {
                long start = System.currentTimeMillis();
                canvas = holder.lockCanvas();
                if(canvas == null)
                    return false;

                canvas.drawColor(mBackgroundColor);

                for(int bundleIndex = 1; bundleIndex < bundleLength + 1; bundleIndex++) {
                    if(mValues[bundleIndex-1] > mMaxValue)
                        mValues[bundleIndex-1] = mMaxValue;
                    //canvas.drawRect(offset + (bundleIndex-1)*constantWidth + interval, mHeight - (mUsedHeight*(mValues[bundleIndex-1]/mMaxValue))*frame/ numOfFrame - mLineWidthPixel, offset + bundleIndex * constantWidth , constantBottom, barPnt);
                    canvas.drawRect(offset + (bundleIndex-1)*constantWidth + interval, mHeight - (mValues[bundleIndex-1] * frame * constantHeight) - mLineWidthPixel, offset + bundleIndex * constantWidth , constantBottom, barPnt);
                }

                holder.unlockCanvasAndPost(canvas);

                long passedTime = System.currentTimeMillis() - start;
                if(passedTime < period/(numOfFrame +1)) {
                    try {
                        Thread.sleep((long)((float)period/(numOfFrame +1))-passedTime);
                    } catch (Exception e) {
                    }
                }
            }
        } else if (numOfFrame == 0) {
            long start = System.currentTimeMillis();
            canvas = holder.lockCanvas();
            if(canvas == null)
                return false;

            canvas.drawColor(mBackgroundColor);

            holder.unlockCanvasAndPost(canvas);

            long passedTime = System.currentTimeMillis() - start;
            if(passedTime < period/(numOfFrame +1)) {
                try {
                    Thread.sleep((long)((float)period/(numOfFrame +1))-passedTime);
                } catch (Exception e) {
                }
            }
        }

        Long m_passedTIme = System.currentTimeMillis() - m_start;
        if(m_passedTIme > (long)(period * 1.1))
            DebugLogger.d(WaveBarManager, "[method : animateTopToZero]passed time is "+ m_passedTIme.toString() + "ms" + " period is " + ((Long)mPeriod).toString() + "ms" + "\nperiod is too short. extra time is nedded.");
        else
            DebugLogger.d(WaveBarManager, "[method : animateTopToZero]passed time is "+ m_passedTIme.toString() + "ms" + " period is " + ((Long)mPeriod).toString() + "ms");
        return true;
    }

    public boolean clearWaveBars() {
        Canvas canvas = mHolder.lockCanvas();
        if(canvas == null)
            return false;

        canvas.drawColor(mBackgroundColor);
        mHolder.unlockCanvasAndPost(canvas);

        return true;
    }

    public void flushBundleList() {
        mBundleList.clear();
    }

    public float getCLValue() {
        return mCLValue;
    }
}
