package com.haifa3D.haifa3d_ble_api.ble

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.Observer
import com.haifa3D.haifa3d_ble_api.notification.createBatteryLevelNotificationChannel

const val BATTERY_LOW_NOTIFICATION_ID = 1000

class BleService() : NotificationService() {
    lateinit var manager: IHandService

    // Binder given to clients
    private val binder = LocalBinder()
    private var isObserving = false
    private var batteryLowNotificationId: Int? = null
    lateinit var bleManager: AppBleManager
    lateinit var realHandService: RealHandService

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): BleService = this@BleService
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent!!)
        return binder
    }

    fun mockConnect() {
        val manager = MockHandService(); this.manager = manager
        manager.connect()
        manager.state.observe(this, Observer {
            if (!it.isConnected) {
                this.manager = realHandService
            }
        })
    }

    private fun setNotificationText(text: CharSequence) {
        var n = notificationBuilder
            .setContentText(text)
            .build()
        updateNotification(n)
    }

    private fun setBatteryPercentageText(percentage: Int) =
        setNotificationText("$percentage % battery")

    private fun onBatteryNotification(notification: BatteryNotification) {
        setBatteryPercentageText(notification.percentage)
        var id = batteryLowNotificationId
        if (notification.percentage > 20 && id != null) {
            batteryLowNotificationId = null
            notificationManager.cancel(id)
        } else if (notification.percentage <= 20 && batteryLowNotificationId == null) {
            id = BATTERY_LOW_NOTIFICATION_ID
            batteryLowNotificationId = id
            val b = prepareNotificationBuilder(notificationManager.createBatteryLevelNotificationChannel(this))
            val msg = "Less than " + notification.percentage.toString() +  " % remaining"
            val tit = "Low battery"
            b.setContentTitle(tit)
            b.setContentText(msg)
            b.setTicker(msg)
            notificationManager.notify(id, b.build())
        }
    }

    override fun onCreate() {
        super.onCreate()
        bleManager = AppBleManager(this)
        realHandService = RealHandService(bleManager)
        realHandService.state.observe(this, Observer {
            if (!isObserving && it.isConnected) {
                manager.batteryService.currentPercentage.observe(this@BleService, Observer {
                    it?.let { onBatteryNotification(it) }
                })
                isObserving = true
            } else if (!it.isConnected) {
                setNotificationText("Not connected")
            }
        })
        manager = realHandService
    }

    override fun onDestroy() {
        bleManager.close()
        super.onDestroy()
    }
}