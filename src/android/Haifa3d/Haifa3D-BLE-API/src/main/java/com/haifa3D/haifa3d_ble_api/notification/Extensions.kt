package com.haifa3D.haifa3d_ble_api.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

const val FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID = "com.gjung.haifa3d.FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID"
const val BATTERY_LEVEL_NOTIFICATION_CHANNEL_ID = "com.gjung.haifa3d.BATTERY_LEVEL_NOTIFICATION_CHANNEL_ID"


fun NotificationManager.createForegroundServiceNotificationChannel(context: Context): NotificationChannel {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "BLE Service"
        val descriptionText ="Handle bluetooth connection in background"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        this.createNotificationChannel(mChannel)
        return mChannel
    }
    throw IllegalStateException("This method should just be called from Android O or above.")
}

fun NotificationManager.createBatteryLevelNotificationChannel(context: Context): NotificationChannel {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Low Battery"
        val descriptionText = "Notify about low battery levels"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(BATTERY_LEVEL_NOTIFICATION_CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        this.createNotificationChannel(mChannel)
        return mChannel
    }
    throw IllegalStateException("This method should just be called from Android O or above.")
}
