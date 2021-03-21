package com.haifa3D.haifa3d_ble_api.ble

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.haifa3D.haifa3d_ble_api.model.HandAction
import com.haifa3D.haifa3d_ble_api.model.decodeHandAction
import no.nordicsemi.android.ble.livedata.state.ConnectionState
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.util.*

interface IHandService {
    val batteryService: IBatteryLevelService
    val directExecuteService: IDirectExecuteService
    val presetService: IPresetService
    val triggerService: ITriggerService
    val configurationService: IConfigurationService

    val state: LiveData<ConnectionState>
    fun connect(device: BluetoothDevice)
    fun disconnect()

    val isConnected
        get() = state.value == ConnectionState.Ready

    val connectedAddress: String?
}

class RealHandService(private val bleManager: AppBleManager) : IHandService {
    override val batteryService: IBatteryLevelService
        get() = bleManager.batteryService
    override val directExecuteService: IDirectExecuteService
        get() = bleManager.directExecuteService
    override val presetService: IPresetService
        get() = bleManager.presetService
    override val triggerService: ITriggerService
        get() = bleManager.triggerService
    override val configurationService: IConfigurationService
        get() = bleManager.configurationService
    override val state: LiveData<ConnectionState>
        get() = bleManager.state
    override val isConnected: Boolean
        get() = bleManager.isConnected
    override val connectedAddress: String?
        get() = bleManager.bluetoothDevice?.address

    override fun connect(device: BluetoothDevice) {
        bleManager.connect(device)
            .retry(3, 100)
            .useAutoConnect(false)
            .enqueue()
    }

    override fun disconnect() {
        bleManager.disconnect()
            .enqueue()
    }
}

@ExperimentalUnsignedTypes
class MockHandService : IHandService {
    override val batteryService: IBatteryLevelService =
        object : IBatteryLevelService {
            override val currentPercentage = MutableLiveData<BatteryNotification?>(null)
        }

    override val directExecuteService: IDirectExecuteService =
        object : IDirectExecuteService {
            override fun executeAction(action: HandAction) {
            }
        }

    override val presetService: IPresetService =
        object : IPresetService {
            private val presets = mutableMapOf<Int, HandAction>()

            init {
                for (x in 0..11)
                    presets[x] = HandAction.Empty
            }

            override suspend fun writePreset(presetNumber: Int, action: HandAction) {
                presets[presetNumber] = action
            }

            override suspend fun readPreset(presetNumber: Int): HandAction? =
                // easy way to deep clone
                presets[presetNumber]?.toBytes()?.toList()?.toUByteArray()?.decodeHandAction()
        }

    override val triggerService: ITriggerService =
        object : ITriggerService {
            override fun trigger(presetNumber: Int) {
            }
        }

    @ExperimentalUnsignedTypes
    override val configurationService: IConfigurationService =
        object : IConfigurationService {
            override val fields: List<IConfigField>
                get() = listOf(
                    object : IConfigField {
                        override val uuid: UUID = UUID.fromString("00000000-1111-2222-3333-444444444443")
                        override val caption: String = "Demo Configuraions:"
                        override val content: LiveData<String> = MutableLiveData("Some value")
                        override val canEdit: Boolean
                            get() = false
                    },
                    object : IConfigField {
                        override val uuid: UUID = UUID.fromString("00000000-1111-2222-3333-444444444443")
                        override val caption: String = "Demo Read-Only Config"
                        override val content: LiveData<String> = MutableLiveData("Some value")
                        override val canEdit: Boolean
                            get() = false
                    },
                    object : IByteConfigField {
                        override val uuid: UUID = UUID.fromString("00000000-1111-2222-3333-444444444444")
                        override val caption: String = "Demo Config Value"
                        override val value = MutableLiveData(0.toUByte())
                        override val content = Transformations.map(value) { it.toString() }

                        override suspend fun setValue(value: UByte) {
                            this.value.postValue(value)
                        }

                        override fun read() {
                        }
                    },
                    object : IBooleanConfigField {
                        override val uuid: UUID = UUID.fromString("00000000-1111-2222-3333-444444444445")
                        override val caption: String = "Debugging Mode"
                        override val value = MutableLiveData(false)
                        override val content = Transformations.map(value) { if (it) "Enabled" else "Disabled" }

                        override suspend fun setValue(value: Boolean) {
                            this.value.postValue(value)
                        }

                        override fun read() {
                        }
                    },
                    object : ITriggerConfigField {
                        override val uuid: UUID = UUID.fromString("00000000-1111-2222-3333-444444444446")
                        override val caption: String = "Demo Configuration Trigger"
                        override val content = MutableLiveData("Click to trigger")
                        override suspend fun trigger() {
                        }
                    }
                )

            override fun readAllValues() {
            }
        }

    override val state: LiveData<ConnectionState>
        get() = conState

    override val connectedAddress: String?
        get() = if(isConnected) "MOCK-DEVICE" else null

    private val conState = MutableLiveData<ConnectionState>()

    fun connect() {
        conState.postValue(ConnectionState.Connecting)
        conState.postValue(ConnectionState.Ready)
    }

    override fun connect(device: BluetoothDevice) {
        connect()
    }

    override fun disconnect() {
        conState.postValue(ConnectionState.Disconnected(ConnectionObserver.REASON_SUCCESS))
    }

}