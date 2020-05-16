package com.gjung.haifa3d.ble

import android.bluetooth.BluetoothDevice

interface BatteryObserver {
    fun onBatteryValueReceived(device: BluetoothDevice, percentage: Int)
}