package com.gjung.haifa3d

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
        // currently we have 5 motors (#0 - #4). in theory we could support up to 8.
        if (configId < 0 || configId > 20) //  || motor > 7)
            throw IllegalArgumentException("The configId has to be in [0, 19].")
        else
            UUID.fromString("e0198003-7544-42c1-10${String.format("%02x", configId)}-b24344b6aa70")
}