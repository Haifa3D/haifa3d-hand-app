package com.gjung.haifa3d.profile

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.gjung.haifa3d.Uuids
import com.gjung.haifa3d.model.HandAction
import no.nordicsemi.android.ble.livedata.ObservableBleManager
import no.nordicsemi.android.log.ILogSession
import no.nordicsemi.android.log.LogContract
import no.nordicsemi.android.log.LogSession
import no.nordicsemi.android.log.Logger


open class AppBleManager(context: Context) : BatteryManager(context) {
    private var logSession: ILogSession? = null
    private var supported: Boolean = false

    private var directExecuteCharacteristic: BluetoothGattCharacteristic? = null

    override fun getGattCallback(): BatteryManagerGattCallback {
        return HandBleManagerGattCallback()
    }

    /**
     * Sets the log session to be used for low level logging.
     * @param session the session, or null, if nRF Logger is not installed.
     */
    fun setLogger(session: ILogSession?) {
        logSession = session
    }

    override fun log(priority: Int, message: String) {
        // The priority is a Log.X constant, while the Logger accepts it's log levels.
        Logger.log(logSession, LogContract.Log.Level.fromPriority(priority), message)
    }

    override fun shouldClearCacheWhenDisconnected(): Boolean {
        return !supported
    }

    protected inner class HandBleManagerGattCallback : BatteryManagerGattCallback() {
        override fun onDeviceDisconnected() {
            super.onDeviceDisconnected()
            directExecuteCharacteristic = null
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            supported = determineIsSupported(gatt)
            return supported
        }

        private fun determineIsSupported(gatt: BluetoothGatt): Boolean {
            val srv = gatt.getService(Uuids.HandDirectExecuteService) ?: return false

            val directExecuteCharacteristic = srv.getCharacteristic(Uuids.ExecuteOnWriteCharacteristic) ?: return false
            if (directExecuteCharacteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE == 0) return false

            return true;
        }
    }

    fun executeAction(action: HandAction) {
        if (directExecuteCharacteristic == null)
            throw IllegalStateException("No supported device connected")

        log(Log.VERBOSE, "Executing action...")
        writeCharacteristic(directExecuteCharacteristic, action.toBytes().toList().toByteArray())
    }
}