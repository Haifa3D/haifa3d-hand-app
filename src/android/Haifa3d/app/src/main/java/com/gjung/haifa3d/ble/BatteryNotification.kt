package com.gjung.haifa3d.ble

import android.bluetooth.BluetoothDevice

data class BatteryNotification(val device: BluetoothDevice, val percentage: Int)