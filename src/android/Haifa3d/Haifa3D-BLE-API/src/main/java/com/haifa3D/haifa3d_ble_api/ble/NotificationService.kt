package com.haifa3D.haifa3d_ble_api.ble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.haifa3D.haifa3d_ble_api.R

import com.haifa3D.haifa3d_ble_api.notification.createForegroundServiceNotificationChannel


abstract class NotificationService() : LifecycleService() {
    protected lateinit var notificationManager: NotificationManager
    val notificationId: Int by lazy {
        hashCode()
    }
    val notificationBuilder: NotificationCompat.Builder by lazy {
        preparePersistentNotificationBuilder()
    }

    companion object {
        @JvmStatic private val StopServiceAction: String = "STOP_SERVICE"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        showNotification()
    }

    override fun onDestroy() {
        // Cancel the persistent notification.
        stopForeground(true)

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
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

    private fun newNotificationBuilder(channel: NotificationChannel? = null) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = channel ?: notificationManager.createForegroundServiceNotificationChannel(this)
            NotificationCompat.Builder(this, channel.id)
        } else {
            NotificationCompat.Builder(this)
        }

    fun prepareNotificationBuilder(channel: NotificationChannel): NotificationCompat.Builder =
        prepareNotificationBuilder(newNotificationBuilder(channel))

    fun
            prepareNotificationBuilder(builder: NotificationCompat.Builder = newNotificationBuilder()): NotificationCompat.Builder {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(), 0
        )

        // Set the info for the views that show in the notification panel.
        return builder
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_baseline_sports_handball_24) // the status icon
            .setWhen(System.currentTimeMillis()) // the time stamp
            .setContentIntent(contentIntent) // The intent to send when the entry is clicked
    }

    private fun preparePersistentNotificationBuilder(builder: NotificationCompat.Builder = newNotificationBuilder()): NotificationCompat.Builder {
        val b = prepareNotificationBuilder(builder)

        val serviceIntent = Intent(this, BleService::class.java)
        serviceIntent.action = StopServiceAction
        val closeIntent = PendingIntent.getService(applicationContext, 0, serviceIntent, 0)

        val text = "Haifa3D"
        return b
            .setTicker(text) // the status text
            .setContentTitle(text) // the label of the entry
            .setContentText(text) // the contents of the entry
            .addAction(R.drawable.ic_bluetooth_disabled, "Close", closeIntent)
    }

    fun updateNotification(notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }

    private fun showNotification() {
        val notification = notificationBuilder.build()

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        // notifMgr.notify(R.string.notification_ble_title, notification)

        startForeground(notificationId, notification)
    }
}
