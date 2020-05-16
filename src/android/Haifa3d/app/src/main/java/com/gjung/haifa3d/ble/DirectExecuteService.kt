package com.gjung.haifa3d.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.gjung.haifa3d.Uuids
import com.gjung.haifa3d.model.HandAction

class DirectExecuteService(manager: BleManagerAccessor) : GattHandler(manager) {
    private var directExecuteCharacteristic: BluetoothGattCharacteristic? = null

    fun executeAction(action: HandAction) {
        if (!manager.isConnected)
            throw IllegalStateException("No device connected")
        if (directExecuteCharacteristic == null)
            throw IllegalStateException("No supported device connected")

        manager.log(Log.VERBOSE, "Executing action...")
        manager.writeCharacteristic(directExecuteCharacteristic!!, action.toBytes().toList().toByteArray())
            .enqueue()
    }

    override fun onDeviceDisconnected() {
        directExecuteCharacteristic = null
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(Uuids.HandDirectExecuteService)
        if (service != null) {
            directExecuteCharacteristic = service.getCharacteristic(Uuids.ExecuteOnWriteCharacteristic)
        }
        return directExecuteCharacteristic != null
    }
}