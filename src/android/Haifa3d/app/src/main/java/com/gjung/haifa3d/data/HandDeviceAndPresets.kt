package com.gjung.haifa3d.data

import androidx.room.Embedded
import androidx.room.Relation

data class HandDeviceAndPresets (
    @Embedded val handDevice: HandDevice,
    @Relation(parentColumn = "hand_device_id", entityColumn = "hand_device_id")
    val presets: List<Preset> = emptyList()
)