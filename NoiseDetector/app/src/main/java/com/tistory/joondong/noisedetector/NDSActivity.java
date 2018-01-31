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

 * ******************************************************************************************************************************************** *
 * ***************************************************************** BleProfile *************************************************************** *
 * ******************************************************************************************************************************************** *
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * */

package com.tistory.joondong.noisedetector;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;

import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.tistory.joondong.noisedetector.utils.IngTimer;
import com.tistory.joondong.wavedata.WaveDataView;
import com.tistory.joondong.wavedata.components.CriticalLine;

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;

public class NDSActivity extends BleProfileServiceReadyActivity<NDSService.NDSBinder> implements NDSService.DNValueCallback, CriticalLine.ExternalCLValueListener {
    NDSService.NDSBinder mService;

    private static WaveDataView mWaveDataView;
    private Switch mBroadcastSwitch;
    private Switch mVibrationSwitch;
    private ImageView mVibrationImg;

    private SharedPreferences mBackup;
    private SharedPreferences.Editor mBackupEditor;
    private final String mRecentBroadcastStatus = "RecentBroadcastStatus";
    private boolean mInteranlRecentBroadcastStatus;
    private final String mRecentVibrationStatus = "RecentVibrationStatus";

    private final String mBroadcastTurnOnMessage = "Requesting turn on the noise detector";
    private final String mBroadcastTurnedOnMessage = "The Noise Detector is turned on.";
    private final String mBroadcastTurnOffMessage = "Requesting turn off the noise detector";
    private final String mBroadcastTurnedOffMessage = "The Noise Detector is turned off.";
    private final String mBroadcastFailedMessage = "The peer didn't respond.";

    private byte mRecentBroadcastRequest;

    private IngTimer mBroadcastIngTimer;

    private static boolean mIsConnected;
    private static boolean mBroadcastSwitchInitialRequest;

    // This class listen the CriticalLine.CLValueListener to get mCLValue.
    @Override
    public void getCLValue(float value) {
        if(mService != null)
            mService.setCLValue(value);
    }

    @Override
    protected void onCreateView(final Bundle savedInstanceState) {
        setContentView(R.layout.layout_noisedetector);
        setGUI();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onInitialize(final Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, makeIntentFilter());
    }

    /**
     * Called when user press CONNECT or DISCONNECT button. See layout files -> onClick attribute.
     */
    public void onConnectClicked(final View view) {
        /*if (!isDeviceConnected())
            internalSetBroadcast(false);*/
        if(mIsConnected && mService != null)
            mService.setUserDIsconnect(true);
        super.onConnectClicked(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        mWaveDataView = null; // TODO : 이상 없음?
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void setDefaultUI() {
        mWaveDataView.ignoreReceive(false);
    }

    @Override
    protected void onServiceBinded(final NDSService.NDSBinder binder) {
        mService = getService();
        mService.registerDNValueCallback(this);
        if(mWaveDataView != null)
            mService.setCLValue(mWaveDataView.getCLValue());
    }

    @Override
    protected void onServiceUnbinded() {
        // not used
    }

    @Override
    protected int getLoggerProfileTitle() {
        return R.string.nds_feature_title;
    }

    @Override
    protected int getAboutTextId() {
        return R.string.nds_about_text;
    }

    @Override
    protected int getDefaultDeviceName() {
        return R.string.nds_default_name;
    }

    @Override
    protected UUID getFilterUUID() {
        //return NDSManager.ND_SERVICE_UUID;
        return null;
    }

    @Override
    protected Class<? extends BleProfileService> getServiceClass() {
        return NDSService.class;
    }

    @Override
    public void onDeviceConnected(final BluetoothDevice device) {
        mIsConnected = true;
        if(mService != null)
            mService.setUserDIsconnect(false);

        super.onDeviceConnected(device);
    }

    @Override
    public void onDeviceDisconnected(final BluetoothDevice device) {
        mIsConnected = false;
        mBroadcastSwitch.setEnabled(false);
        super.onDeviceDisconnected(device);
    }

    @Override
    public void onLinklossOccur(final BluetoothDevice device) {
        super.onLinklossOccur(device);
        //mBroadcastSwitch.setEnabled(false);
    }

    @Override
    public void onServicesDiscovered(final BluetoothDevice device, boolean optionalServicesFound) {
        // this may notify user or show some views
        mBroadcastSwitch.setEnabled(true); // OnCheckedChangeListener of the broadcast switch is not called automatically, although this was set to true in initializeSwitches.
        if(mInteranlRecentBroadcastStatus) {
            // OnCheckedChangeListener of the broadcast switch is called immediately although it is disabled.
            // this is enabled in onDeviceConnected.
            mBroadcastSwitchInitialRequest = true;
            mBroadcastSwitch.setChecked (true); // If disconnected on active state, OnCheckedChangeListener of mBroadcastSwitch is not called from seceond connection, because it just can detect change state.
            if(mBroadcastSwitchInitialRequest == true) { // So, call OnCheckedChangeListener's internal function directly.
                onBroadcastSwitchStatus(true);
                mBroadcastSwitchInitialRequest = false;
            }
        }
        // parent method is empty
    }

    @Override
    public void onBackPressed() {
        mWaveDataView.clearData();
        super.onBackPressed();
    }

    private void setGUI() {
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
                200,
                20,
                true,
                // Warning settings
                Color.RED, // Warning color
                1000, // Warning time
                3); // Twinkling number
        mWaveDataView.ignoreReceive(false);
        waveDataFrame.addView(mWaveDataView);
        mWaveDataView.setCLValueListener(this);

        mBackup = this.getSharedPreferences("Backup", 0);
        mBackupEditor = mBackup.edit();
        mInteranlRecentBroadcastStatus = mBackup.getBoolean(mRecentBroadcastStatus, false);
        if(mInteranlRecentBroadcastStatus)
            mRecentBroadcastRequest = NDSManager.mNoiseDetectorON;
        else
            mRecentBroadcastRequest = NDSManager.mNoiseDetectorOFF;
        initializeSwitches();
    }

    /* This method initialize the Buttons */
    public void initializeSwitches() {
        mBroadcastSwitch = (Switch)findViewById(R.id.broadcast);
        mBroadcastSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onBroadcastSwitchStatus(isChecked);
            }
        });
        // mBroadcastIngTimer = new IngTimer(this, 5000, 300, mBroadcastSwitch);
        // TODO : 화면 방향전환시 getService(), isDeviceConnected() 작동하지 않는다. BleProfileServiceReadyActivity의 mService도 null이기 때문..
        // TODO : 따라서 mInteranlRecentBroadcastStatus 이용
        if(mIsConnected) {
            mBroadcastSwitch.setEnabled(true);
            if(mInteranlRecentBroadcastStatus) {
                mBroadcastSwitch.setChecked(true);
            }
        }
        else
            mBroadcastSwitch.setEnabled(false);

        mVibrationSwitch = (Switch)findViewById(R.id.vibration);
        mVibrationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if(mWaveDataView.setVibrator(true)) {
                        mVibrationImg.setImageResource(R.drawable.vibration_p);
                        mBackupEditor.putBoolean(mRecentVibrationStatus, true);
                        mBackupEditor.commit();
                    } else {
                        setVibrationSwitch(!isChecked, "Graphic manager is not activated");
                    }
                } else {
                    if(mWaveDataView.setVibrator(false)) {
                        mVibrationImg.setImageResource(R.drawable.vibration_n);
                        mBackupEditor.putBoolean(mRecentVibrationStatus, false);
                        mBackupEditor.commit();
                    } else {
                        setVibrationSwitch(!isChecked, "Graphic manager is not activated");
                    }
                }
            }
        });
        mVibrationImg = (ImageView)findViewById(R.id.vibration_img);
        if(mBackup.getBoolean(mRecentVibrationStatus, false)) {
            mVibrationSwitch.setChecked(true);
            mVibrationImg.setImageResource(R.drawable.vibration_p);
        }
        else {
            mVibrationSwitch.setChecked(false);
            mVibrationImg.setImageResource(R.drawable.vibration_n);
        }
    }

    private void onBroadcastSwitchStatus(boolean isChecked) {
        if(isChecked) {
            mBackupEditor.putBoolean(mRecentBroadcastStatus, true);
            mBackupEditor.commit();
            if(mInteranlRecentBroadcastStatus != true || mBroadcastSwitchInitialRequest == true) {
                mBroadcastIngTimer = new IngTimer(NDSActivity.this, 5000, 300, mBroadcastSwitch); // TODO
                mBroadcastIngTimer.setMessages(mBroadcastTurnOnMessage, mBroadcastFailedMessage,mBroadcastTurnedOnMessage);
                mBroadcastIngTimer.start();
                setBroadcast(isChecked);
                notifyBroadcastStatusToService(isChecked);
            }
            mRecentBroadcastRequest = NDSManager.mNoiseDetectorON;
            mInteranlRecentBroadcastStatus = true;
            mBroadcastSwitchInitialRequest = false;
        } else {
            mWaveDataView.clearData();
            mBackupEditor.putBoolean(mRecentBroadcastStatus, false);
            mBackupEditor.commit();
            if(mInteranlRecentBroadcastStatus != false){
                mBroadcastIngTimer = new IngTimer(NDSActivity.this, 5000, 300, mBroadcastSwitch); // TODO
                mBroadcastIngTimer.setMessages(mBroadcastTurnOffMessage, mBroadcastFailedMessage, mBroadcastTurnedOffMessage);
                mBroadcastIngTimer.start();
                setBroadcast(isChecked);
                notifyBroadcastStatusToService(isChecked);
            }
            mRecentBroadcastRequest = NDSManager.mNoiseDetectorOFF;
            mInteranlRecentBroadcastStatus = false;
        }
    }

    /* This method turn off WaveDataView and send 'Write Request' to Noise Detector Control Point of a peer. */
    private void setBroadcast(boolean operation) {
        // TODO add 'write request' to Noise Detector Controller(Control Point) Characteristic
        if(mWaveDataView != null) {
            mWaveDataView.ignoreReceive(!operation);
            mWaveDataView.clearData();
        }
        internalSetBroadcast(operation);
    }
    /* This method request write to Noise Detector Control Point Characteristic.
    * But characteristic's information is saved in the NDSService, so should use binder.
    * Thid is used in setBroadcast() method. */
    private void internalSetBroadcast(boolean operation) {
        if(mService != null)
            mService.setBroadcast(operation);
    }

    private void notifyBroadcastStatusToService(boolean status) {
        if(mService != null)
            mService.setBroadcastStatus(status);
    }

    /* This method is used to restore the broadcast switch when WaveDataView::setVibrator() methed is failed. */
    public void setVibrationSwitch(boolean request, String msg) {
        mVibrationSwitch.setChecked(request);
        mVibrationImg.setImageResource(R.drawable.vibration_n);
        mBackupEditor.putBoolean(mRecentVibrationStatus, request);
        mBackupEditor.commit();
    }


    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();

            if (NDSService.BROADCAST_NDS_CONTROLPOINT.equals(action)) {
                // TODO
                // terminate progressbar before it time out if EXTRA_CURRENT_VALUE value equals mCurrentRequestedCPValue.
                // this block treats result of requestNDControlPoint(onOff) method.
                final byte[] values = intent.getByteArrayExtra(NDSService.EXTRA_RESULT_OPCODE);
                if((values[0] == mRecentBroadcastRequest) && (values[1] == NDSManager.mNDSResultSucceed) && mBroadcastIngTimer != null)
                    mBroadcastIngTimer.breakTimer();
            }
        }
    };
    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NDSService.BROADCAST_NDS_DETECTED);
        intentFilter.addAction(NDSService.BROADCAST_NDS_CONTROLPOINT);
        return intentFilter;
    }

    public void onDNValueReceived(float[] values, float maxValue) {
        if(mWaveDataView != null)
            mWaveDataView.animateBars(values, maxValue);
    }
}
