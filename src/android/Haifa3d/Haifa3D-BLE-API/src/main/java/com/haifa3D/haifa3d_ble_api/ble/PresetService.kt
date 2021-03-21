package com.haifa3D.haifa3d_ble_api.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.haifa3D.haifa3d_ble_api.Uuids
import com.haifa3D.haifa3d_ble_api.model.HandAction
import com.haifa3D.haifa3d_ble_api.model.decodeHandAction
import com.haifa3D.haifa3d_ble_api.readBytesAsync
import com.haifa3D.haifa3d_ble_api.sendSuspend

const val HandSupportedNumberOfPresets = 12


interface IPresetService {
    suspend fun writePreset(presetNumber: Int, action: HandAction)
    suspend fun readPreset(presetNumber: Int): HandAction?
}

class PresetService(manager: BleManagerAccessor) : GattHandler(manager), IPresetService {
    private var presetActionByteCharacteristic: Array<BluetoothGattCharacteristic?> = Array(HandSupportedNumberOfPresets) { null }

    override suspend fun writePreset(presetNumber: Int, action: HandAction) {
        if (presetNumber < 0 || presetNumber >= HandSupportedNumberOfPresets)
            throw IllegalArgumentException("presetNumber must be in [0..${HandSupportedNumberOfPresets - 1}]")
        if (!manager.isConnected)
            throw IllegalStateException("No device connected")
        val characteristic = presetActionByteCharacteristic[presetNumber]
            ?: throw IllegalStateException("No supported device connected")

        manager.log(Log.VERBOSE, "Writing preset $presetNumber...")
        manager.writeCharacteristic(characteristic, action.toBytes().toList().toUByteArray().toByteArray())
            .sendSuspend()
    }

    override suspend fun readPreset(presetNumber: Int): HandAction? {
        if (presetNumber < 0 || presetNumber >= HandSupportedNumberOfPresets)
            throw IllegalArgumentException("presetNumber must be in [0..${HandSupportedNumberOfPresets - 1}]")
        if (!manager.isConnected)
            throw IllegalStateException("No device connected")
        val characteristic = presetActionByteCharacteristic[presetNumber]
            ?: throw IllegalStateException("No supported device connected")

        manager.log(Log.VERBOSE, "Readig preset $presetNumber...")
        val bytes = manager.readCharacteristic(characteristic)
                           .readBytesAsync()
                           ?.toUByteArray()

        return bytes?.decodeHandAction()
    }

    override fun onDeviceDisconnected() {
        presetActionByteCharacteristic = Array(HandSupportedNumberOfPresets) { null }
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(Uuids.HandPresetService) ?: return false

        for (i in 0 until HandSupportedNumberOfPresets)
            presetActionByteCharacteristic[i] = service.getCharacteristic(Uuids.PresetActionBytesCharacteristic(i.toByte()))
                ?: return false

        return true
    }
}