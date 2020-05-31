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
    private fun ConfigurationMotorSpecificValue(motor: Byte, valueId: String): UUID =
        // currently we have 5 motors (#0 - #4). in theory we could support up to 8.
        if (motor < 0 || motor > 4) //  || motor > 7)
            throw IllegalArgumentException("The motor number has to be in [0, 4].")
        else if (valueId == "0")
            throw IllegalArgumentException("valueId can not be \"0\". Otherwise, the characteristic uuid would be == the service uuid.")
        else
            UUID.fromString("e0198003-7544-42c1-10${String.format("%01x", motor)}${valueId}-b24344b6aa70")

    fun ConfigurationLowTorqueValueCharacteristic(motor: Byte) =
        ConfigurationMotorSpecificValue(motor, "1")
    fun ConfigurationLowTorqueSlopeValueCharacteristic(motor: Byte) =
        ConfigurationMotorSpecificValue(motor, "2")
    fun ConfigurationHighTorqueValueCharacteristic(motor: Byte) =
        ConfigurationMotorSpecificValue(motor, "3")
    fun ConfigurationHighTorqueSlopeValueCharacteristic(motor: Byte) =
        ConfigurationMotorSpecificValue(motor, "4")

    val ConfigurationTorqueMeasureStartMsCharacteristic: UUID = UUID.fromString("e0198003-7544-42c1-0101-b24344b6aa70")
    val ConfigurationWindowsWidthFilterCharacteristic: UUID = UUID.fromString("e0198003-7544-42c1-0102-b24344b6aa70")
}