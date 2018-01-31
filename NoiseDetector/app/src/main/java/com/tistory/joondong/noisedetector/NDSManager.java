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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;

/**
 * Created by isp40 on 2017-10-12.
 */

public class NDSManager extends BleManager<NDSManagerCallbacks> {

    /* Noise Detector Service UUID */
    public final static UUID ND_SERVICE_UUID = UUID.fromString("f6738d00-0994-4967-bdf9-5e7702990a50");

    /* Noise Detector Characteristic UUID */
    public final static UUID ND_DNV_CHARACTERISTIC_UUID = UUID.fromString("f6738d01-0994-4967-bdf9-5e7702990a50");
    public final static UUID ND_CONTROLPOINT_UUID = UUID.fromString("f6738d02-0994-4967-bdf9-5e7702990a50");

    private final static UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic mDNVCharacteristic;
    private BluetoothGattCharacteristic mNDCPCharacteristic;

    public static final byte mNoiseDetectorOFF = 1;
    public static final byte mNoiseDetectorON = 2;

    public static final byte mNDSResultFailed = 1;
    public static final byte mNDSResultSucceed = 2;

    public NDSManager(final Context context) {
        super(context);
    }

    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    public void setBroadcast(boolean operation) {
        if ((mNDCPCharacteristic != null) && (mDNVCharacteristic != null)) {
            final BluetoothGattCharacteristic characteristic = mNDCPCharacteristic;
            final BluetoothGattDescriptor descriptor = mDNVCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);

            byte[] value = new byte[2];
            value[1] = 0;

            // This reduce the peer's overload.
            if (operation) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                value[0] = mNoiseDetectorON;
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                value[0] = mNoiseDetectorOFF;
            }
            Logger.i(mLogSession, "setBradcast : value[0] = " + ((Byte)value[0]).toString());
            characteristic.setValue(value);
            writeCharacteristic(characteristic);
            writeDescriptor(descriptor);
        }
    }
    /**
     * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
     */
    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

        @Override
        protected Deque<Request> initGatt(BluetoothGatt gatt) {
            final LinkedList<Request> requests = new LinkedList<>();
            requests.add(Request.newEnableNotificationsRequest(mDNVCharacteristic));
            requests.add(Request.newEnableIndicationsRequest(mNDCPCharacteristic));
            return requests;
        }

        @Override
        protected boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(ND_SERVICE_UUID);
            if (service != null) {
                mDNVCharacteristic = service.getCharacteristic(ND_DNV_CHARACTERISTIC_UUID);
                mNDCPCharacteristic = service.getCharacteristic(ND_CONTROLPOINT_UUID);
            }
            return (mDNVCharacteristic != null) && (mNDCPCharacteristic != null);
        }

        @Override
        protected void onDeviceDisconnected() {
            mDNVCharacteristic = null;
            mNDCPCharacteristic = null;
        }

        @Override
        protected void onCharacteristicIndicated(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            if (ND_CONTROLPOINT_UUID.equals(characteristic.getUuid())) {
                byte[] value = characteristic.getValue();
                if(value.length == 2)
                    mCallbacks.onNDCPResponseReceived(value);
            }
            if(ND_DNV_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                mCallbacks.onDNValueReceived(characteristic.getValue());
            }
        }

        @Override
        protected void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            if(ND_DNV_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                mCallbacks.onDNValueReceived(characteristic.getValue());
            }
        }
    };

    @Override
    protected boolean shouldAutoConnect() {
        return true;
    }

    public boolean areCharacteristicsNull() {
        return (mDNVCharacteristic == null) || (mNDCPCharacteristic == null);
    }
}
