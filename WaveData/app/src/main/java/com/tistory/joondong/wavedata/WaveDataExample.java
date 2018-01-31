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

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import java.util.Random;

public class WaveDataExample extends AppCompatActivity{
    WaveDataView mWaveDataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wavedata_example);

        ViewGroup waveDataFrame  = (ViewGroup)findViewById(R.id.wavedataframe);
        mWaveDataView = WaveDataView.getView(this,
                // Common settings
                Color.WHITE,
                8, // The ratio of extra height when the orientation is horizontal mode.
                11, // The ratio VerticalLine occupy when the orientation is horizontal mode.
                8, // The ratio of extra height when the orientation is vertical mode.
                6, // The ratio VerticalLine occupy when the orientation is vertical mode.
                255,
                false,
                // VerticalLine settings
                1,
                Color.BLACK,
                "V",
                (float)1.8,
                true,
                1,
                9, // font size when the orientation is horizontal mode.
                10, // font size when the orientation is vertical mode.
                Color.BLACK,
                // CriticalLne Settings
                Color.RED,
                2,
                20,
                // WaveBars settings
                0xFF008000,
                true,
                0xFF00FF00,
                8,
                500,
                20,
                true,
                // Warning settings
                Color.RED, // Warning color
                1000, // Warning time
                3); // Twinkling number
        mWaveDataView.ignoreReceive(false);
        waveDataFrame.addView(mWaveDataView);
    }

    public void onClick(View v) {
        Random random  = new Random();

        switch(v.getId()) {
            case R.id.btn1: {
                float[] values = new float[500];
                for (int j = 0; j < values.length; j++)
                    values[j] = 155 * random.nextFloat();
                mWaveDataView.animateBars(values);
            }
            break;

            case R.id.btn2: {
                float[] values = new float[20];
                for (int j = 0; j < 20; j++)
                    values[j] = 255 * random.nextFloat();
                mWaveDataView.animateBars(values);
            }
                break;

            case R.id.btn3 :
                mWaveDataView.toggleVibrator();
                break;

            case R.id.btn4 :
                mWaveDataView.clearData();
                break;

            case R.id.btn5 :
                mWaveDataView.ignoreReceive(false);
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        mWaveDataView.clearData();
        super.onBackPressed();
    }
}