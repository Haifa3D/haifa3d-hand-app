package com.example.haifa3d_ble_api.ble

import android.bluetooth.BluetoothGatt
import no.nordicsemi.android.ble.BleManager

abstract class GattHandler(val manager: BleManagerAccessor) {
    abstract fun onDeviceDisconnected()
    abstract fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean
    open fun onDeviceReady() {}
    open fun initialize() {}
}