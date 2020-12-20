package com.gjung.haifa3d.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.gjung.haifa3d.R
import com.gjung.haifa3d.Uuids
import com.gjung.haifa3d.sendSuspend
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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

interface ITriggerConfigField: IConfigField {
    override val canEdit: Boolean
        get() = false
    suspend fun trigger()
}

interface IHeaderConfigField: IConfigField {
    override val canEdit: Boolean
        get() = false
    suspend fun trigger()
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
        fields.add(HeaderConfigField(
            Uuids.ConfigurationValueCharacteristic(14),context.getString(R.string.configuration_basic),""))

        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(0),
            context.getString(R.string.configuration_ltv)))

        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(1),
            context.getString(R.string.configuration_htv)))


        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(4),
            context.getString(R.string.configuration_lts)))

        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(5),
            context.getString(R.string.configuration_hts)))

        fields.add(TriggerConfigField(
            Uuids.ConfigurationTriggerCharacteristic(1),
            context.getString(R.string.configuration_trigger_reset_config),
            context.getString(R.string.configuration_trigger_reset_config_descr)))

        fields.add(TriggerConfigField(
            Uuids.ConfigurationTriggerCharacteristic(0),
            context.getString(R.string.configuration_trigger_reset_presets),
            context.getString(R.string.configuration_trigger_reset_presets_descr)))

        fields.add(HeaderConfigField(
            Uuids.ConfigurationValueCharacteristic(13),context.getString(R.string.configuration_advanced),""))

        fields.add(TriggerConfigField(
            Uuids.ConfigurationTriggerCharacteristic(1),
            context.getString(R.string.configuration_trigger_reset_config),
            context.getString(R.string.configuration_trigger_reset_config_descr)))


        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(2),
            context.getString(R.string.configuration_ts)))

        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(3),
            context.getString(R.string.configuration_ww)))


        // config [8..12]
        for (motor in 0..4) {
            fields.add(ByteConfigField(
                Uuids.ConfigurationValueCharacteristic((8 + motor).toByte()),
                context.getString(R.string.configuration_tf, motor)))
        }

        // config [13..16] undefined

        // clarify config 17, time unit; would make UI misleading
        // or requires better implementation on the app's side

        fields.add(ByteConfigField(
            Uuids.ConfigurationValueCharacteristic(18),
            context.getString(R.string.configuration_dv)))

        fields.add(BooleanConfigField(
            Uuids.ConfigurationValueCharacteristic(19),
            context.getString(R.string.configuration_deb),
            context.getString(R.string.config_value_true),
            context.getString(R.string.config_value_false)))

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

    inner class HeaderConfigField(
        override val uuid: UUID,
        override val caption: String,
        description: String,
        override var characteristic: BluetoothGattCharacteristic? = null
    ) : IHeaderConfigField, IBleConfigField {
        override suspend fun trigger() {
            this@ConfigurationService.manager
                .writeCharacteristic(characteristic!!, arrayOf<Byte>(1).toByteArray())
                .sendSuspend()
        }

        override val content = MutableLiveData(description)
    }


}