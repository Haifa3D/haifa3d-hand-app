package com.haifa3D.haifa3d_ble_api

import android.content.Intent
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import kotlin.coroutines.*
import com.haifa3D.haifa3d_ble_api.ble.*
import com.haifa3D.haifa3d_ble_api.model.HandAction
import kotlinx.coroutines.*

/**
 *
 * This class represents an instance of the BLE library.
 * @author asaf azar
 * @author niv levi
 * @property bleService - an instance of the BleService which all other services will be derived from it
 * @property triggerService - an instance of the BLE triggerService which is used to trigger a specific preset
 * @property batteryService - an instance of the BLE batteryService which is used to handle all battery related actions
 * @property presetService - an instance of the BLE presetsSerivce which is used to read/write presets to the controller
 */
class BleAPICommands() {
    private var presetService: IPresetService? = null
    private var triggerService: ITriggerService? = null
    private var batteryService: IBatteryLevelService? = null
    private var bleService: BleService? = null
    private var presetsList = mutableListOf<HandAction?>()
    private lateinit var connection: ServiceConnection

    /**
     *This interface should be implemented by the user which intends to use the library functions
     */
    interface IBleListener {
        fun onConnected(bleService: BleService)
        fun onDisconnected()
    }

    /**
     * this function is used to bind the BLE service to the user's IBleListener object
     * @param callback:IBleListener - an object which implements the interface above
     * @param context: Context - the context in which the service will run
     * @param intent: Intent - an intent which is used to to communicate with the background BLE Service.
     */
    fun bind(callback:IBleListener, context: Context, intent: Intent){
         connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as BleService.LocalBinder
                bleService = binder.getService()
                callback.onConnected(bleService!!)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                bleService = null
                callback.onDisconnected()
            }
        }

        context.startService(intent)
        Intent(context, BleService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT)
        }

    }

    /**
     * this function is used to unbind the BLE service once the user is done interacting with the service
     * @param context: Context - the context in which the service will run
     */
    fun unbind(context: Context){
        context.unbindService(connection)
    }

    /**
     * this function is used to connect the BLE service to a specific Bluetooth device
     * @param device: BluetoothDevice - the Bluetooth device (hand controller) which the service will interact with
     */
    fun connect(device: BluetoothDevice){
        bleService?.manager?.connect(device)
        if(bleService?.manager?.isConnected == false){
            bleService?.bleManager?.log(Log.VERBOSE, " couldn't connect to BT device")
        }

    }

    /**
     * this function is used to terminate the connection between the BLE service and the Bluetooth device (hand controller)
     */
    fun disconnect(){
        bleService?.manager?.disconnect()
    }

    /**
     * this function is used to trigger a specific preset by preset_number. please note that the controller can store up to 12 presets
     * @param preset_number: Int - an integer between 0 - 11
     */
    fun Hand_activation_by_preset(preset_number: Int){
        triggerService = bleService!!.manager.triggerService
        try {
            triggerService?.trigger(preset_number)
        }
        catch (e: Exception){
            print("execption was thrown")
        }

    }

    /**
     * this function is used to extract a specific preset from the controller
     * @param preset_number: Int - an integer between 0 - 11
     * @return HandAction - an object which contains a list of HandMovement(describe a single hand movement) objects
     */
    suspend fun Extract_preset(preset_number: Int):HandAction? {
        presetService = bleService!!.manager.presetService
        var returendPreset: HandAction? = null
        try{
            returendPreset = presetService?.readPreset(preset_number)

        }
        catch (e: Exception)
        {
            print("execption was thrown")
        }
        return returendPreset
    }

    /**
     * this function is used to extract the battery level
     * @return Int - an Integer between 1 -  100 which the describe hte battery level in percentage
     */
    fun Extract_battery_status():Int{
        batteryService = bleService!!.manager.batteryService
        val currentPercentage = batteryService?.currentPercentage
        return currentPercentage?.value!!.get_precentage()
    }
}
