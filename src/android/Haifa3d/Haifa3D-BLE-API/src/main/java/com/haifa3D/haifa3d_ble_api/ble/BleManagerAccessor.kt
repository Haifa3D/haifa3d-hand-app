package com.haifa3D.haifa3d_ble_api.ble

import android.bluetooth.BluetoothGattCharacteristic
import no.nordicsemi.android.ble.ReadRequest
import no.nordicsemi.android.ble.ValueChangedCallback
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.data.Data

interface BleManagerAccessor {
    fun setNotificationCallback(characteristic: BluetoothGattCharacteristic): ValueChangedCallback
    fun enableNotifications(characteristic: BluetoothGattCharacteristic): WriteRequest
    fun disableNotifications(characteristic: BluetoothGattCharacteristic): WriteRequest
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, data: Data): WriteRequest
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, data: ByteArray): WriteRequest
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, data: ByteArray, offset: Int, length: Int): WriteRequest
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic): ReadRequest
    fun log(priority: Int, message: String)
    val isConnected: Boolean
}