package com.haifa3D.haifa3d_ble_api

import java.lang.IllegalArgumentException
import java.util.*

object Uuids {
    val HandDirectExecuteService: UUID = UUID.fromString("e0198000-7544-42c1-0000-b24344b6aa70")
    val ExecuteOnWriteCharacteristic: UUID = UUID.fromString("e0198000-7544-42c1-0001-b24344b6aa70")


    val HandPresetService: UUID = UUID.fromString("e0198001-7544-42c1-0000-b24344b6aa70")
    fun PresetActionBytesCharacteristic(presetNumber: Byte): UUID =
        UUID.fromString("e0198001-7544-42c1-10${String.format("%02x", presetNumber)}-b24344b6aa70")


    val HandTriggerService: UUID = UUID.fromString("e0198002-7544-42c1-0000-b24344b6aa70")
    val TriggerPresetNumberCharacteristic: UUID = UUID.fromString("e0198002-7544-42c1-0001-b24344b6aa70")


    val HandConfigurationService: UUID = UUID.fromString("e0198003-7544-42c1-0000-b24344b6aa70")
    fun ConfigurationValueCharacteristic(configId: Byte): UUID =
        if (configId < 0 || configId > 20)
            throw IllegalArgumentException("The configId has to be in [0, 19].")
        else
            UUID.fromString("e0198003-7544-42c1-10${String.format("%02x", configId)}-b24344b6aa70")

    fun ConfigurationTriggerCharacteristic(configId: Byte): UUID =
        if (configId < 0 || configId > 1)
            throw IllegalArgumentException("The configId has to be in [0, 1].")
        else
            UUID.fromString("e0198003-7544-42c1-01${String.format("%02x", configId)}-b24344b6aa70")
}