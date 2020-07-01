package com.gjung.haifa3d.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HandDeviceDao {
    @Query("SELECT * FROM hand_device WHERE address = :address")
    fun getByAddress(address: String): HandDevice?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertHandDevice(device: HandDevice): Long
}