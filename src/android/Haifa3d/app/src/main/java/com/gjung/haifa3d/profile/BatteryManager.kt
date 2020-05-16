package com.gjung.haifa3d.profile

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.gjung.haifa3d.ble.BatteryObserver
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelDataCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import no.nordicsemi.android.log.LogContract
import java.util.*

abstract class BatteryManager(context: Context) : ObservableBleManager(context) {
    /** Battery Service UUID.  */
    private val BATTERY_SERVICE_UUID: UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
    /** Battery Level characteristic UUID.  */
    private val BATTERY_LEVEL_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb")

    private var batteryLevelCharacteristic: BluetoothGattCharacteristic? = null
    /** Last received Battery Level value.  */
    /**
     * Returns the last received Battery Level value.
     * The value is set to null when the device disconnects.
     * @return Battery Level value, in percent.
     */
    var batteryLevel: Int? = null

    var batteryObserver: BatteryObserver? = null

    abstract override fun getGattCallback(): BatteryManagerGattCallback

    private val batteryLevelDataCallback: DataReceivedCallback =
        object : BatteryLevelDataCallback() {
            override fun onBatteryLevelChanged(device: BluetoothDevice, batteryLevel: Int) {
                log(
                    LogContract.Log.Level.APPLICATION,
                    "Battery Level received: $batteryLevel%"
                )
                this@BatteryManager.batteryLevel = batteryLevel
                batteryObserver?.onBatteryValueReceived(device, batteryLevel)
            }

            override fun onInvalidDataReceived(device: BluetoothDevice, data: Data) {
                log(Log.WARN, "Invalid Battery Level data received: $data")
            }
        }

    fun readBatteryLevelCharacteristic() {
        if (isConnected) {
            readCharacteristic(batteryLevelCharacteristic)
                .with(batteryLevelDataCallback)
                .fail { _, _ ->
                    log(Log.WARN, "Battery Level characteristic not found")
                }
                .enqueue()
        }
    }

    fun enableBatteryLevelCharacteristicNotifications() {
        if (isConnected) {
            // If the Battery Level characteristic is null, the request will be ignored
            setNotificationCallback(batteryLevelCharacteristic)
                .with(batteryLevelDataCallback)
            enableNotifications(batteryLevelCharacteristic)
                .done {
                    log(Log.INFO, "Battery Level notifications enabled")
                }
                .fail { _, _ ->
                    log(Log.WARN, "Battery Level characteristic not found")
                }
                .enqueue()
        }
    }

    /**
     * Disables Battery Level notifications on the Server.
     */
    fun disableBatteryLevelCharacteristicNotifications() {
        if (isConnected) {
            disableNotifications(batteryLevelCharacteristic)
                .done {
                    log(Log.INFO, "Battery Level notifications disabled")
                }
                .enqueue()
        }
    }

    protected abstract inner class BatteryManagerGattCallback : BleManagerGattCallback() {
        override fun initialize() {
            readBatteryLevelCharacteristic()
            enableBatteryLevelCharacteristicNotifications()
        }

        override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(BATTERY_SERVICE_UUID)
            if (service != null) {
                batteryLevelCharacteristic = service.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID)
            }
            return batteryLevelCharacteristic != null
        }

        override fun onDeviceDisconnected() {
            batteryLevelCharacteristic = null
            batteryLevel = null
        }
    }
}