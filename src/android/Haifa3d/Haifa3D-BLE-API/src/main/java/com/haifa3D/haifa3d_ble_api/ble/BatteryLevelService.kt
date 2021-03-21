package com.haifa3D.haifa3d_ble_api.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelDataCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.log.LogContract
import java.util.*

interface IBatteryLevelService {
    val currentPercentage: LiveData<BatteryNotification?>
}

class BatteryLevelService(manager: BleManagerAccessor) : GattHandler(manager), IBatteryLevelService {
    /** Battery Service UUID.  */
    private val BATTERY_SERVICE_UUID: UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
    /** Battery Level characteristic UUID.  */
    private val BATTERY_LEVEL_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

    private lateinit var batteryLevelCharacteristic: BluetoothGattCharacteristic

    override val currentPercentage: LiveData<BatteryNotification?>
        get() = mutableNotification
    private val mutableNotification = MutableLiveData<BatteryNotification?>()

    private val batteryLevelDataCallback: DataReceivedCallback =
        object : BatteryLevelDataCallback() {
            override fun onBatteryLevelChanged(device: BluetoothDevice, batteryLevel: Int) {
                manager.log(
                    LogContract.Log.Level.APPLICATION,
                    "Battery Level received: $batteryLevel%"
                )
                mutableNotification.postValue(BatteryNotification(device, batteryLevel))
            }

            override fun onInvalidDataReceived(device: BluetoothDevice, data: Data) {
                manager.log(Log.WARN, "Invalid Battery Level data received: $data")
            }
        }


    fun readBatteryLevelCharacteristic() {
        if (!manager.isConnected)
            throw IllegalStateException("Not connected to a device.")

        manager.readCharacteristic(batteryLevelCharacteristic)
            .with(batteryLevelDataCallback)
            .fail { _, _ ->
                manager.log(Log.WARN, "Battery Level characteristic not found")
            }
            .enqueue()
    }

    fun enableBatteryLevelCharacteristicNotifications() {
        if (!manager.isConnected)
            throw IllegalStateException("Not connected to a device.")

        manager.setNotificationCallback(batteryLevelCharacteristic)
            .with(batteryLevelDataCallback)
        manager.enableNotifications(batteryLevelCharacteristic)
            .done {
                manager.log(Log.INFO, "Battery Level notifications enabled")
            }
            .fail { _, _ ->
                manager.log(Log.WARN, "Battery Level characteristic not found")
            }
            .enqueue()
    }

    /**
     * Disables Battery Level notifications on the Server.
     */
    fun disableBatteryLevelCharacteristicNotifications() {
        if (!manager.isConnected)
            throw IllegalStateException("Not connected to a device.")

        manager.disableNotifications(batteryLevelCharacteristic)
            .done {
                manager.log(Log.INFO, "Battery Level notifications disabled")
            }
        .enqueue()
    }

    override fun initialize() {
        readBatteryLevelCharacteristic()
        enableBatteryLevelCharacteristicNotifications()
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(BATTERY_SERVICE_UUID)
        if (service != null) {
            batteryLevelCharacteristic = service.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
        }
        return batteryLevelCharacteristic != null
    }

    override fun onDeviceDisconnected() {
        mutableNotification.postValue(null)
    }
}