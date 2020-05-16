package com.gjung.haifa3d.ble

import android.content.Intent
import android.os.Binder
import android.os.IBinder

class BleService : NotificationService() {
    lateinit var manager: AppBleManager

    // Binder given to clients
    private val binder = LocalBinder()

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): BleService = this@BleService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        manager = AppBleManager(this)
    }

    override fun onDestroy() {
        manager.close()
        super.onDestroy()
    }
}