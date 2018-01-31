/* ******************************************************************************************************************************************** *
 * ************************************************************* Noise Detector *************************************************************** *
 * ******************************************************************************************************************************************** *
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
 * */

package com.tistory.joondong.noisedetector.utils;

import android.content.Context;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by isp40 on 2017-10-24.
 */

public class IngTimer extends Thread {
    private Toast mToast;
    private long mTime;
    private String mIngMsg;
    private String mTimeoutMsg;
    private String mBreakMsg;
    private long mVarInt;
    private int mCount = 0;

    private boolean mBreakKey = false;
    private Button mButton;

    private Handler mHandler = new Handler();

    private final String[] mIng = {".", "..", "..."};

    private Runnable mIngMsgRunnable = new Runnable() {
        @Override
        public void run() {
            mToast.setText(mIngMsg + mIng[mCount++]);
            if(mCount > 2) {
                mCount = 0;
            }
            mToast.show();
        }
    };

    public IngTimer(Context context, long time, long varInt) {
        mToast = Toast.makeText(context, "-", Toast.LENGTH_SHORT);
        mTime = time;
        if(time > varInt)
            mVarInt = varInt;
        else
            mVarInt = time;
    }

    public IngTimer(Context context, long time, long varInt, Button button) {
        mToast = Toast.makeText(context, "-", Toast.LENGTH_SHORT);
        mTime = time;
        if(time > varInt)
            mVarInt = varInt;
        else
            mVarInt = time;
        mButton = button;
    }

    public IngTimer(Context context, long time, String ingMsg, long varInt, String timeoutMsg) {
        mToast = Toast.makeText(context, "-", Toast.LENGTH_SHORT);
        mTime = time;
        mIngMsg = ingMsg;
        if(time > varInt)
            mVarInt = varInt;
        else
            mVarInt = time;
        mTimeoutMsg = timeoutMsg;
    }

    public IngTimer(Context context, Button button, long time, String ingMsg, long varInt, String timeoutMsg) {
        mToast = Toast.makeText(context, "-", Toast.LENGTH_SHORT);
        mButton = button;
        mTime = time;
        mIngMsg = ingMsg;
        if(time > varInt)
            mVarInt = varInt;
        else
            mVarInt = time;
        mTimeoutMsg = timeoutMsg;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long end;

        if(mButton != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mButton.setEnabled(false);
                }
            });
        }


        mHandler.post(mIngMsgRunnable);
        do {
            try { Thread.sleep(mVarInt); } catch (Exception e) {}
            end = System.currentTimeMillis();
            if(mIngMsg != null)
                mHandler.post(mIngMsgRunnable);
        }while ((end - start < mTime) && mBreakKey == false);

        if(mBreakKey == true) {
            if(mBreakMsg != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mToast.setText(mBreakMsg);
                        mToast.show();
                    }
                });
                try { Thread.sleep(3000); } catch (Exception e) {}
            }
        }else if(mTimeoutMsg != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mToast.setText(mTimeoutMsg);
                    mToast.show();
                }
            });
            try { Thread.sleep(3000); } catch (Exception e) {}
        }

        if(mButton != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mButton.setEnabled(true);
                }
            });
        }

        // Initialize
        mBreakKey = false;
        mCount = 0;
    }

    public void breakTimer() {
        mBreakKey = true;
    }

    public void setMessages(String ingMsg, String timeoutMsg, String breakMsg) {
        mIngMsg = ingMsg;
        mTimeoutMsg = timeoutMsg;
        mBreakMsg = breakMsg;
    }

    public void setIngMessage(String ingMsg) {
        mIngMsg = ingMsg;
    }

    public void setbreakMessage(String timeoutMsg) {
        mTimeoutMsg = timeoutMsg;
    }

    public void setTimeoutMessage(String breakMsg) {
        mBreakMsg = breakMsg;
    }

    public void setButton(Button button) {
        mButton = button;
    }

    public void setMessagesAndButton(String ingMsg, String timeoutMsg, Button button) {
        mIngMsg = ingMsg;
        mTimeoutMsg = timeoutMsg;
        mButton = button;
    }
}
