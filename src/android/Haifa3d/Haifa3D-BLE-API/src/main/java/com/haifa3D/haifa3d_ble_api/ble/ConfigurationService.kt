package com.haifa3D.haifa3d_ble_api.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.haifa3D.haifa3d_ble_api.Uuids
import com.haifa3D.haifa3d_ble_api.sendSuspend
import java.util.*

interface IConfigurationService {
    val fields: List<IConfigField>
    fun readAllValues()
}

interface IConfigField {
    val uuid: UUID
    val caption: String
    val content: LiveData<String>
    val canEdit: Boolean
}

interface IReadableConfigField : IConfigField {
    fun read()
}

interface IBleConfigField: IConfigField {
    var characteristic: BluetoothGattCharacteristic?
}

@ExperimentalUnsignedTypes
interface IByteConfigField: IReadableConfigField {
    override val canEdit: Boolean
        get() = true
    val value: LiveData<UByte>
    suspend fun setValue(value: UByte)
}

@ExperimentalUnsignedTypes
interface IBooleanConfigField: IReadableConfigField {
    override val canEdit: Boolean
        get() = true
    val value: LiveData<Boolean>
    suspend fun setValue(value: Boolean)
}

interface IWindowWidthConfigField: IReadableConfigField {
    override val canEdit: Boolean
        get() = true
    val value: LiveData<Int>
    suspend fun setValue(value: Int)
}

interface ITriggerConfigField: IConfigField {
    override val canEdit: Boolean
        get() = false
    suspend fun trigger()
}

interface IHeaderConfigField: IConfigField {
    override val canEdit: Boolean
        get() = true
    val value: LiveData<Int>
    suspend fun setValue(value: Int)
}

@ExperimentalUnsignedTypes
class ConfigurationService(manager: BleManagerAccessor, private val context: Context
) : GattHandler(manager), IConfigurationService {
    override val fields = mutableListOf<IBleConfigField>()

    override fun onDeviceDisconnected() {
        for (field in fields) {
            field.characteristic = null
        }
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(Uuids.HandConfigurationService)
            ?: return true // we support hands that can not be configured too
        for (field in fields) {
            service.getCharacteristic(field.uuid)?.let {
                field.characteristic = it
            }
        }
        return true
    }

    init {
        // i've added (asaf) 2 new fields for "basic"/"adavnced" configurations using config ids: 14/15 which are unused
        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(14),
            "Basic Configuration"))

        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(0),
            "Low Torque Value"))

        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(1),
            "High Torque Value"))


        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(4),
            "Low Torque Slope Value"))

        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(5),
            "High Torque Slope Value"))

        fields.add(TriggerConfigField(
            Uuids.ConfigurationTriggerCharacteristic(1),
            "Reset Configuration",
           "Tap to reset configuration to factory defaults"))

        fields.add(TriggerConfigField(
            Uuids.ConfigurationTriggerCharacteristic(0),
            "Reset Presets",
            "Tap to reset presets to factory defaults"))


        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(15),
            "Advanced Configuration"))


        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(2),
            "Torque Measure Start"))

        fields.add(WindowWidthConfigField(
            Uuids.ConfigurationValueCharacteristic(3),
            "Window Width Filter"))


        // config [6..10]
        for (motor in 0..4) {
            fields.add(ByteConfigField(
                Uuids.ConfigurationValueCharacteristic((6 + motor).toByte()),
                "Threshold Factor Motor "+ motor.toString() + " "))
        }

        // config [13..16] undefined

        // clarify config 17, time unit; would make UI misleading
        // or requires better implementation on the app's side

        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(18),
            "Threshold Value Unit"))

        fields.add(BooleanConfigField(
            Uuids.ConfigurationValueCharacteristic(19),
            "Debugging<",
           "Enabled",
           "Disabled"))

    }

    override fun readAllValues() {
        for (field in fields.filterIsInstance<IReadableConfigField>()) {
            field.read()
        }
    }

    @ExperimentalUnsignedTypes
    inner class ByteConfigField(
        override val uuid: UUID,
        override val caption: String,
        override val value: MutableLiveData<UByte> = MutableLiveData<UByte>(),
        override var characteristic: BluetoothGattCharacteristic? = null
    ) : IByteConfigField, IBleConfigField {
        override suspend fun setValue(value: UByte) {
            this@ConfigurationService.manager
                .writeCharacteristic(characteristic!!, arrayOf(value.toByte()).toByteArray())
                .sendSuspend()
        }

        override fun read() {
            characteristic?.let {
                this@ConfigurationService.manager
                    .readCharacteristic(it)
                    .with { _, data ->
                        try {
                            value.postValue(data.getByte(0)!!.toUByte())
                        } catch (t: Throwable) {
                            // maybe the config data on the hand isn't initialized
                        }
                    }
                    .enqueue()
            }
        }

        override val content = Transformations.map(value) { it.toString() }
    }

    inner class BooleanConfigField(
        override val uuid: UUID,
        override val caption: String,
        private val contentForTrue: String,
        private val contentForFalse: String,
        override val value: MutableLiveData<Boolean> = MutableLiveData<Boolean>(),
        override var characteristic: BluetoothGattCharacteristic? = null
    ) : IBooleanConfigField, IBleConfigField {
        override suspend fun setValue(value: Boolean) {
            this@ConfigurationService.manager
                .writeCharacteristic(characteristic!!, arrayOf<Byte>(if (value) 1 else 0).toByteArray())
                .sendSuspend()
        }

        override fun read() {
            characteristic?.let {
                this@ConfigurationService.manager
                    .readCharacteristic(it)
                    .with { _, data ->
                        try {
                            value.postValue(data.getByte(0) == 1.toByte())
                        } catch (t: Throwable) {
                            // maybe the config data on the hand isn't initialized
                        }
                    }
                    .enqueue()
            }
        }

        override val content = Transformations.map(value) { if (it) contentForTrue else contentForFalse }
    }

    inner class TriggerConfigField(
        override val uuid: UUID,
        override val caption: String,
        description: String,
        override var characteristic: BluetoothGattCharacteristic? = null
    ) : ITriggerConfigField, IBleConfigField {
        override suspend fun trigger() {
            this@ConfigurationService.manager
                .writeCharacteristic(characteristic!!, arrayOf<Byte>(1).toByteArray())
                .sendSuspend()
        }

        override val content = MutableLiveData(description)
    }

    inner class WindowWidthConfigField(
        override val uuid: UUID,
        override val caption: String,
        override val value: MutableLiveData<Int> = MutableLiveData<Int>(),
        override var characteristic: BluetoothGattCharacteristic? = null
    ) : IWindowWidthConfigField, IBleConfigField {
        override suspend fun setValue(value: Int) {
            this@ConfigurationService.manager
                .writeCharacteristic(characteristic!!, arrayOf(value.toByte()).toByteArray())
                .sendSuspend()
        }

        override fun read() {
            characteristic?.let {
                this@ConfigurationService.manager
                    .readCharacteristic(it)
                    .with { _, data ->
                        try {
                            value.postValue(data.getByte(0)!!.toInt())
                        } catch (t: Throwable) {
                            // maybe the config data on the hand isn't initialized
                        }
                    }
                    .enqueue()
            }
        }

        override val content = Transformations.map(value) { it.toString() }
    }



}