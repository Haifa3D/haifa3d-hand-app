package com.gjung.haifa3d.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PresetDao {
    @Transaction
    @Query("SELECT preset.* FROM preset " +
            "INNER JOIN hand_device ON preset.hand_device_id = hand_device.hand_device_id " +
            "WHERE hand_device.address = :handDeviceAddress")
    fun getHandDevicePresets(handDeviceAddress: String): LiveData<List<Preset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: Preset): Long
}