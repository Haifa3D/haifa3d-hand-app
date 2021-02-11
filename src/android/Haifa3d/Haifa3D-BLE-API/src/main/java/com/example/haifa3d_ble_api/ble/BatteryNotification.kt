package com.example.haifa3d_ble_api.ble

import android.bluetooth.BluetoothDevice

data class BatteryNotification(val device: BluetoothDevice, val percentage: Int)