package com.gjung.haifa3d.profile

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gjung.haifa3d.ble.BatteryObserver

const val BROADCAST_BATTERY_LEVEL = "com.gjung.haifa3d.BROADCAST_BATTERY_LEVEL"
const val EXTRA_BATTERY_LEVEL = "com.gjung.haifa3d.EXTRA_BATTERY_LEVEL"

abstract class BatteryService: BleService(),
    BatteryObserver {
    abstract override fun initializeManager(): AppBleManager

    override fun onBatteryValueReceived(device: BluetoothDevice, percentage: Int) {
        val broadcast = Intent(BROADCAST_BATTERY_LEVEL)
        broadcast.putExtra(EXTRA_DEVICE, device)
        broadcast.putExtra(EXTRA_BATTERY_LEVEL, percentage)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }
}