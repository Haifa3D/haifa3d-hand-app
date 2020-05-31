package com.gjung.haifa3d.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ReadRequest
import no.nordicsemi.android.ble.ValueChangedCallback
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.livedata.ObservableBleManager

open class AppBleManager(context: Context) : ObservableBleManager(context) {
    private lateinit var accessor: Accessor
    lateinit var batteryService: BatteryLevelService
    lateinit var directExecuteService: DirectExecuteService
    lateinit var presetService: PresetService
    lateinit var triggerService: TriggerService

    override fun getGattCallback(): BleManagerGattCallback {
        accessor = Accessor()
        batteryService = BatteryLevelService(accessor)
        directExecuteService = DirectExecuteService(accessor)
        presetService = PresetService(accessor)
        triggerService = TriggerService(accessor)
        return CompositeHandler(batteryService, directExecuteService, presetService, triggerService)
    }

    private inner class CompositeHandler(vararg val handlers: GattHandler) : BleManagerGattCallback() {
        override fun onDeviceDisconnected() {
            handlers.forEach {
                it.onDeviceDisconnected()
            }
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean =
            handlers.all { it.isRequiredServiceSupported(gatt) }

        override fun initialize() {
            super.initialize()
            handlers.forEach { it.initialize() }
        }

        override fun onDeviceReady() {
            super.onDeviceReady()
            handlers.forEach { it.onDeviceReady() }
        }
    }

    private inner class Accessor : BleManagerAccessor {
        override fun setNotificationCallback(characteristic: BluetoothGattCharacteristic): ValueChangedCallback =
            this@AppBleManager.setNotificationCallback(characteristic)

        override fun enableNotifications(characteristic: BluetoothGattCharacteristic): WriteRequest =
            this@AppBleManager.enableNotifications(characteristic)

        override fun disableNotifications(characteristic: BluetoothGattCharacteristic): WriteRequest  =
            this@AppBleManager.disableNotifications(characteristic)

        override fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, data: Data): WriteRequest  =
            this@AppBleManager.writeCharacteristic(characteristic, data)

        override fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, data: ByteArray): WriteRequest  =
            this@AppBleManager.writeCharacteristic(characteristic, data)

        override fun writeCharacteristic(
            characteristic: BluetoothGattCharacteristic,
            data: ByteArray,
            offset: Int,
            length: Int
        ): WriteRequest  =
            this@AppBleManager.writeCharacteristic(characteristic, data, offset, length)

        override fun readCharacteristic(characteristic: BluetoothGattCharacteristic): ReadRequest  =
            this@AppBleManager.readCharacteristic(characteristic)

        override fun log(priority: Int, message: String)  =
            this@AppBleManager.log(priority, message)

        override val isConnected: Boolean
            get() = this@AppBleManager.isConnected

    }
}