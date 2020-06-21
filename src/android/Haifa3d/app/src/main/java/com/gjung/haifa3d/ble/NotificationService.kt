package com.gjung.haifa3d.ble

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.gjung.haifa3d.ConnectedActivity
import com.gjung.haifa3d.R
import com.gjung.haifa3d.notification.createBleNotificationChannel


abstract class NotificationService : Service() {
    private lateinit var notifMgr: NotificationManager

    companion object {
        @JvmStatic private val StopServiceAction: String = "STOP_SERVICE"
    }

    override fun onCreate() {
        super.onCreate()
        notifMgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        showNotification()
    }

    override fun onDestroy() {
        // Cancel the persistent notification.
        stopForeground(true)

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == StopServiceAction) {
            stopService()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    open fun stopService() {
        stopForeground(true)
        stopSelf()
    }

    /**
     * Show a notification while this service is running.
     */
    private fun showNotification() { // In this sample, we'll use the same text for the ticker and the expanded notification
        val text = getText(R.string.notification_ble_title)

        // The PendingIntent to launch our activity if the user selects this notification
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, ConnectedActivity::class.java), 0
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notifMgr.createBleNotificationChannel(this)
            NotificationCompat.Builder(this, channel.id)
        } else {
            NotificationCompat.Builder(this)
        }

        val serviceIntent = Intent(this, BleService::class.java)
        serviceIntent.action = StopServiceAction
        val closeIntent = PendingIntent.getService(applicationContext, 0, serviceIntent, 0)

        // Set the info for the views that show in the notification panel.
        val notification: Notification = builder
            .setSmallIcon(R.drawable.ic_device_blinky) // the status icon
            .setTicker(text) // the status text
            .setWhen(System.currentTimeMillis()) // the time stamp
            .setContentTitle(text) // the label of the entry
            .setContentText(text) // the contents of the entry
            .setContentIntent(contentIntent) // The intent to send when the entry is clicked
            .addAction(R.drawable.ic_bluetooth_disabled, "Close", closeIntent)
            .build()

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        // notifMgr.notify(R.string.notification_ble_title, notification)

        startForeground(hashCode(), notification)
    }
}
