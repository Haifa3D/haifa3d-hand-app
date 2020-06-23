package com.gjung.haifa3d.ble

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gjung.haifa3d.model.HandAction
import no.nordicsemi.android.ble.livedata.state.ConnectionState
import no.nordicsemi.android.ble.observer.ConnectionObserver

interface IHandService {
    val batteryService: IBatteryLevelService
    val directExecuteService: IDirectExecuteService
    val presetService: IPresetService
    val triggerService: ITriggerService

    val state: LiveData<ConnectionState>
    fun connect(device: BluetoothDevice)
    fun disconnect()

    val isConnected
        get() = state.value == ConnectionState.Ready
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
    override val state: LiveData<ConnectionState>
        get() = bleManager.state

    override val isConnected: Boolean
        get() = bleManager.isConnected

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

            override suspend fun readPreset(presetNumber: Int): HandAction? = presets[presetNumber]
        }

    override val triggerService: ITriggerService =
        object : ITriggerService {
            override fun trigger(presetNumber: Int) {
            }
        }

    override val state: LiveData<ConnectionState>
        get() = conState

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