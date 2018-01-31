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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import com.tistory.joondong.wavedata.components.CriticalLine;

import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;

/**
 * Created by isp40 on 2017-10-12.
 */

public class NDSService extends BleProfileService implements NDSManagerCallbacks{
    // The action and extra value to receive Noise Detector Characteristic's detected values.
    public static final String BROADCAST_NDS_DETECTED = "com.tistory.joondong.noisedetector.BROADCAST_NDS_MEASUREMENT";
    public static final String EXTRA_VALUES = "com.tistory.joondong.noisedetector.EXTRA_NOISE_VALUES"; // not used : chaged from broadcast to interface

    // The action and extra value to receive to receive Noise Detector Control Point's result.
    public static final String BROADCAST_NDS_CONTROLPOINT = "com.tistory.joondong.noisedetector.BROADCAST_NDS_CONTROLPOINT";
    public static final String EXTRA_RESULT_OPCODE = "com.tistory.joondong.noisedetector.EXTRA_RESULT_OPCODE";

    private final static String ACTION_DISCONNECT = "com.tistory.joondong.noisedetector.ACTION_DISCONNECT";

    private final LocalBinder mBinder = new NDSBinder();
    private NDSManager mNDSManager;

    private final static int NOTIFICATION_ID = 267;
    private final static int OPEN_ACTIVITY_REQ = 0;
    private final static int DISCONNECT_REQ = 1;

    private static boolean mBindedForThread;

    private boolean mBroadcastStatus;
    private boolean mUserDisconnect;

    public interface DNValueCallback {
        public void onDNValueReceived(float[] values, float maxValue);
    }
    private static DNValueCallback mDNValueCallback;

    private BleDataWrappingThread mWrappingThread;

    private static float mLatestCLValue;
    private static Vibrator mVibrator;
    public static long[] mVibrationPattern = new long[] {0, 100, 50, 100}; // it is possible to be changed at the compile time.

    /**
     * This local binder is an interface for the bonded activity to operate with the NDS sensor
     */
    public class NDSBinder extends LocalBinder {
        public void setBroadcast(boolean operation) {
            mNDSManager.setBroadcast(operation); // just bridge
        }

        public void setBroadcastStatus(boolean status) {
            if(status) {
                if(mWrappingThread == null) {
                    mWrappingThread = new BleDataWrappingThread();
                    mWrappingThread.start();
                }
            } else {
                if(mWrappingThread != null){
                    mWrappingThread.mHandler.getLooper().quitSafely();
                    mWrappingThread.exitThread();
                    while(mWrappingThread.isExtied() != true) {}
                    mWrappingThread = null;
                }
            }
            mBroadcastStatus = status;
        }

        public void setUserDIsconnect(boolean userDisconnect) {
            mUserDisconnect = userDisconnect;
        }

        public void registerDNValueCallback(DNValueCallback receiveActivity) {
            mDNValueCallback = receiveActivity;
        }

        public void setCLValue(float value) {
            mLatestCLValue = value;
        }
    }

    @Override
    protected LocalBinder getBinder() {
        return mBinder;
    }

    @Override
    protected BleManager<NDSManagerCallbacks> initializeManager() {
        return mNDSManager = new NDSManager(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mVibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DISCONNECT);
        registerReceiver(mDisconnectActionBroadcastReceiver, filter);
    }

    @Override
    public void onDestroy() {
        // when user has disconnected from the sensor, we have to cancel the notification that we've created some milliseconds before using unbindService
        cancelNotification();
        unregisterReceiver(mDisconnectActionBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
        if(mWrappingThread != null)
        {
            if(mBroadcastStatus && !mWrappingThread.isExtied() && !mUserDisconnect) {
                mNDSManager.setBroadcast(true);
            }
        }
        super.onServicesDiscovered(device, optionalServicesFound);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        mBindedForThread = true;
        return super.onBind(intent);
    }

    @Override
    protected void onRebind() {
        cancelNotification();
        mBindedForThread = true;
        mNDSManager.readBatteryLevel();
    }

    @Override
    protected void onUnbind() {
        createNotification(R.string.nds_notification_connected_message, 0);
        mBindedForThread=false;
        mDNValueCallback = null;
    }

    public void onDNValueReceived(byte[] byteValues) {
        Message msg = new Message();
        msg.obj = byteValues;
        if(mWrappingThread != null)
            if(mWrappingThread.isAlive())
                mWrappingThread.mHandler.sendMessage(msg);
    }

    public void onNDCPResponseReceived(byte[] value) {
        // TODO : broadcast this information to NDSActivity.
        final Intent broadcast = new Intent(BROADCAST_NDS_CONTROLPOINT);
        broadcast.putExtra(EXTRA_RESULT_OPCODE, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    /**
     * Creates the notification
     *
     * @param messageResId
     *            message resource id. The message must have one String parameter,<br />
     *            f.e. <code>&lt;string name="name"&gt;%s is connected&lt;/string&gt;</code>
     * @param defaults
     *            signals that will be used to notify the user
     */
    private void createNotification(final int messageResId, final int defaults) {

        final Intent disconnect = new Intent(ACTION_DISCONNECT);
        final PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, disconnect, PendingIntent.FLAG_UPDATE_CURRENT);

        // both activities above have launchMode="singleTask" in the AndroidManifest.xml file, so if the task is already running, it will be resumed
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, OPEN_ACTIVITY_REQ, new Intent(this, NDSActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(getString(no.nordicsemi.android.nrftoolbox.R.string.app_name)).setContentText(getString(messageResId, getDeviceName()));
        builder.setSmallIcon(R.drawable.ic_stat_notify_nds);
        builder.setShowWhen(defaults != 0).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_action_bluetooth, getString(R.string.nds_notification_action_disconnect), disconnectAction));

        final Notification notification = builder.build();
        final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Cancels the existing notification. If there is no active notification this method does nothing
     */
    private void cancelNotification() {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }

    /**
     * This broadcast receiver listens for {@link #ACTION_DISCONNECT} that may be fired by pressing Disconnect action button on the notification.
     */
    private final BroadcastReceiver mDisconnectActionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Logger.i(getLogSession(), "[Notification] Disconnect action pressed");
            if (isConnected())
                getBinder().disconnect();
            else
                stopSelf();
        }
    };

    class BleDataWrappingThread extends Thread {
        //Handler mHandler;
        NDSService.BleDataWrappinghandler mHandler;
        private boolean mExit;
        private boolean mExited;

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new NDSService.BleDataWrappinghandler();
            while(mExit == false)
                Looper.loop();

            mExited = true;
        }

        public void exitThread(){
            mExit = true;
        }

        public boolean isExtied() {
            if(mExited)
                return true;
            else
                return false;
        }
    }

    private static class BleDataWrappinghandler extends Handler {
        public void handleMessage(Message msg){
            byte[] byteValues = (byte[])msg.obj;

            if(mBindedForThread) {
                float[] floatValues = new float[byteValues.length];
                float maxValue = 0;
                for(int i=0; i<byteValues.length; i++) {
                    if(byteValues[i] < 0)
                        floatValues[i] = (float)byteValues[i] + 256;
                    else
                        floatValues[i] = (float)byteValues[i];
                    if(floatValues[i] > maxValue)
                        maxValue = floatValues[i];
                }
                if(mDNValueCallback != null)
                    mDNValueCallback.onDNValueReceived(floatValues, maxValue);
            } else {
                int tmp = 0;
                int maxValue = 0;
                for(int i=0; i<byteValues.length; i++) {
                    if((byteValues[i] < 0) && (tmp = byteValues[i] + 256) > maxValue) {
                        maxValue = tmp;
                    } else if (byteValues[i] > maxValue) {
                        maxValue = byteValues[i];
                    }
                }
                if(maxValue > mLatestCLValue) {
                    mVibrator.vibrate(mVibrationPattern, -1);
                }
            }
        }
    }
}
