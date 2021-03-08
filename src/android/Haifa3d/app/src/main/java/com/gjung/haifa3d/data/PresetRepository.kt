package com.gjung.haifa3d.data

import com.example.haifa3d_ble_api.model.HandAction
//import com.gjung.haifa3d.model.HandAction

class PresetRepository private constructor(private val presetDao: PresetDao, private val deviceDao: HandDeviceDao) {
    fun getHandDevicePresets(handDeviceAddress: String) = presetDao.getHandDevicePresets(handDeviceAddress)
    suspend fun saveHandDevicePreset(handDeviceAddress: String, name: String?, blePresetId: Int, content: HandAction, isStarred: Boolean) {
        var devId = deviceDao.getByAddress(handDeviceAddress)?.handDeviceId
            ?: deviceDao.insertHandDevice(HandDevice(handDeviceAddress))
        presetDao.insertPreset(Preset(devId, name, blePresetId, content, isStarred))
    }

    companion object {
        @Volatile private var instance: PresetRepository? = null
        fun getInstance(presetDao: PresetDao, deviceDao: HandDeviceDao) =
            instance ?: synchronized(this) {
                instance ?: PresetRepository(presetDao, deviceDao).also { instance = it }
            }
    }
}