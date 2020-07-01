package com.gjung.haifa3d.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hand_device",
    indices = [
        Index(value = ["address"], unique = true)
    ]
)
data class HandDevice(
    @ColumnInfo(name = "address") val address: String
)
{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "hand_device_id")
    var handDeviceId: Long = 0
}