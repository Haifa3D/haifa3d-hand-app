package com.gjung.haifa3d.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.haifa3D.haifa3d_ble_api.model.HandAction
//import com.gjung.haifa3d.model.HandAction

@Entity(
    tableName = "preset",
    indices = [
        Index(value = ["hand_device_id", "ble_preset_id"], unique = true)
    ]
)
data class Preset(
    @ColumnInfo(name = "hand_device_id") val handDeviceId: Long,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "ble_preset_id") val blePresetId: Int,
    @ColumnInfo(name = "preset_content", typeAffinity = ColumnInfo.BLOB) val content: HandAction,
    @ColumnInfo(name = "starred") val starred: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "preset_id")
    var presetId: Long = 0
}