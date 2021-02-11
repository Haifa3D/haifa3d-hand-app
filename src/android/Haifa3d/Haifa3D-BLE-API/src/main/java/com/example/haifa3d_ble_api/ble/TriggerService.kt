package com.example.haifa3d_ble_api.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.example.haifa3d_ble_api.Uuids
import com.example.haifa3d_ble_api.model.HandAction
import com.example.haifa3d_ble_api.model.decodeHandAction
import com.example.haifa3d_ble_api.readBytesAsync

interface ITriggerService {
    fun trigger(presetNumber: Int)
}

class TriggerService(manager: BleManagerAccessor) : GattHandler(manager), ITriggerService {
    private var triggerCharacteristic: BluetoothGattCharacteristic? = null

    override fun trigger(presetNumber: Int) {
        if (presetNumber < 0 || presetNumber >= HandSupportedNumberOfPresets)
            throw IllegalArgumentException("presetNumber must be in [0..${HandSupportedNumberOfPresets - 1}]")
        if (!manager.isConnected)
            throw IllegalStateException("No device connected")
        val characteristic = triggerCharacteristic
            ?: throw IllegalStateException("No supported device connected")

        manager.log(Log.VERBOSE, "Triggering preset $presetNumber...")
        manager.writeCharacteristic(characteristic, listOf(presetNumber.toByte()).toByteArray())
            .enqueue()
    }

    override fun onDeviceDisconnected() {
        triggerCharacteristic = null
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(Uuids.HandTriggerService) ?: return false

        triggerCharacteristic = service.getCharacteristic(Uuids.TriggerPresetNumberCharacteristic)
            ?: return false

        return true
    }
}