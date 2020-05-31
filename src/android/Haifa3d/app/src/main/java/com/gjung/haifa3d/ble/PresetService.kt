package com.gjung.haifa3d.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.gjung.haifa3d.Uuids
import com.gjung.haifa3d.model.HandAction
import com.gjung.haifa3d.model.decodeHandAction
import com.gjung.haifa3d.readBytesAsync
import com.gjung.haifa3d.sendSuspend
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.data.Data
import java.lang.IllegalArgumentException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

const val HandSupportedNumberOfPresets = 12

class PresetService(manager: BleManagerAccessor) : GattHandler(manager) {
    private var presetActionByteCharacteristic: Array<BluetoothGattCharacteristic?> = Array(HandSupportedNumberOfPresets) { null }

    suspend fun writePreset(presetNumber: Int, action: HandAction) {
        if (presetNumber < 0 || presetNumber >= HandSupportedNumberOfPresets)
            throw IllegalArgumentException("presetNumber must be in [0..${HandSupportedNumberOfPresets - 1}]")
        if (!manager.isConnected)
            throw IllegalStateException("No device connected")
        val characteristic = presetActionByteCharacteristic[presetNumber]
            ?: throw IllegalStateException("No supported device connected")

        manager.log(Log.VERBOSE, "Writing preset $presetNumber...")
        manager.writeCharacteristic(characteristic, action.toBytes().toList().toByteArray())
            .sendSuspend()
    }

    suspend fun readPreset(presetNumber: Int): HandAction? {
        if (presetNumber < 0 || presetNumber >= HandSupportedNumberOfPresets)
            throw IllegalArgumentException("presetNumber must be in [0..${HandSupportedNumberOfPresets - 1}]")
        if (!manager.isConnected)
            throw IllegalStateException("No device connected")
        val characteristic = presetActionByteCharacteristic[presetNumber]
            ?: throw IllegalStateException("No supported device connected")

        manager.log(Log.VERBOSE, "Readig preset $presetNumber...")
        val bytes = manager.readCharacteristic(characteristic)
                           .readBytesAsync()

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