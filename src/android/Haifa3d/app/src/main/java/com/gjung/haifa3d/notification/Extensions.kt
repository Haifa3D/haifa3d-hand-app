package com.gjung.haifa3d.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import com.gjung.haifa3d.R


const val BLE_NOTIFICATION_CHANNEL_ID = "com.gjung.haifa3d.BLE_NOTIFICATION_CHANNEL_ID"

fun NotificationManager.createBleNotificationChannel(context: Context): NotificationChannel {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.notification_ble_channel_name)
        val descriptionText = context.getString(R.string.notification_ble_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(BLE_NOTIFICATION_CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        this.createNotificationChannel(mChannel)
        return mChannel
    }
    throw IllegalStateException("This method should just be called from Android O or above.")
}