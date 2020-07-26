package com.gjung.haifa3d.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gjung.haifa3d.R
import com.gjung.haifa3d.Uuids
import com.gjung.haifa3d.sendSuspend
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@ExperimentalUnsignedTypes
interface IConfigurationService {
    val fields: List<IConfigField>
    fun readAllValues()
}

@ExperimentalUnsignedTypes
interface IConfigField {
    val uuid: UUID
    val caption: String
    val value: LiveData<UByte>
    suspend fun setValue(value: UByte)
    fun read()
}

@ExperimentalUnsignedTypes
class ConfigurationService(manager: BleManagerAccessor, private val context: Context
) : GattHandler(manager), IConfigurationService {
    override val fields = mutableListOf<ConfigField>()

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
        for (motor in 0..4) {
            fields.add(ConfigField(
                Uuids.ConfigurationLowTorqueValueCharacteristic(motor.toByte()),
                context.getString(R.string.configuration_ltv, motor + 1)))
            fields.add(ConfigField(
                Uuids.ConfigurationLowTorqueSlopeValueCharacteristic(motor.toByte()),
                context.getString(R.string.configuration_ltsv, motor + 1)))
            fields.add(ConfigField(
                Uuids.ConfigurationHighTorqueValueCharacteristic(motor.toByte()),
                context.getString(R.string.configuration_htv, motor + 1)))
            fields.add(ConfigField(
                Uuids.ConfigurationHighTorqueSlopeValueCharacteristic(motor.toByte()),
                context.getString(R.string.configuration_htsv, motor + 1)))
        }
        fields.add(ConfigField(
            Uuids.ConfigurationTorqueMeasureStartMsCharacteristic,
            context.getString(R.string.configuration_tmst)))
        fields.add(ConfigField(
            Uuids.ConfigurationWindowsWidthFilterCharacteristic,
            context.getString(R.string.configuration_wwf)))
    }

    override fun readAllValues() {
        for (field in fields) {
            field.read()
        }
    }

    @ExperimentalUnsignedTypes
    inner class ConfigField(
        override val uuid: UUID,
        override val caption: String,
        override val value: MutableLiveData<UByte> = MutableLiveData<UByte>(),
        var characteristic: BluetoothGattCharacteristic? = null
    ) : IConfigField {
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
    }
}