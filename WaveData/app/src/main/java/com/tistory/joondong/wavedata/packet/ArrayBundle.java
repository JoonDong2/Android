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

package com.tistory.joondong.wavedata.packet;

/**
 * Created by isp40 on 2017-10-06.
 */

public class ArrayBundle {
    public float[] mValues;
    public float mMaxValue;

    public ArrayBundle(float[] floatBundle) {
        mValues = floatBundle;

        float maxValue = 0;
        for(int i=0; i<floatBundle.length; i++) {
            if(floatBundle[i] > maxValue)
                maxValue = floatBundle[i];
        }

        mMaxValue = maxValue;
    }

    public ArrayBundle(byte[] byteBundle) {
        mValues = new float[byteBundle.length];
        float maxValue = 0;
        for(int i=0; i<byteBundle.length; i++) {
            if(byteBundle[i] < 0)
                mValues[i] = (float)byteBundle[i] + 256;
            else
                mValues[i] = (float)byteBundle[i];
            if(mValues[i] > maxValue)
                maxValue = mValues[i];
        }
        mMaxValue = maxValue;
    }

    public ArrayBundle(float[] floatBundle, float maxValue) {
        mValues = floatBundle;
        mMaxValue = maxValue;
    }
}