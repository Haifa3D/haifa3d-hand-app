package com.example.haifa3d_ble_api

import android.content.Intent
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context

import android.content.ServiceConnection
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.util.Log

import com.example.haifa3d_ble_api.ble.BleService
import com.example.haifa3d_ble_api.ble.IPresetService
import com.example.haifa3d_ble_api.ble.ITriggerService
import com.example.haifa3d_ble_api.ble.IBatteryLevelService
import com.example.haifa3d_ble_api.model.HandAction

class BleAPICommands() {
    private var presetService: IPresetService? = null
    private var triggerService: ITriggerService? = null
    private var battery_service: IBatteryLevelService? = null
    private var bleService: BleService? = null
    private lateinit var connection: ServiceConnection

    interface IBleListener { // THINK ABOUT MOVING INTERFACE DECLERATION TO LIBRARY
        fun onConnected(bleService: BleService)
        fun onDisconnected()
    }

    // here we bind to the android service and return an instance of ServiceConnection // do we need to pass an interface object that implements onSerivce methods
    fun bind(callback:IBleListener,context: Context) {
         val connection = object : ServiceConnection {
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
//        startService(Intent(this, BleService::class.java))
        Intent(context, BleService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT)
        }
    }

    fun unbind(context: Context){
        context.unbindService(connection)

    }

    //here we connect the service(after binding) with a specific device
    fun connect(device: BluetoothDevice){

        bleService?.manager?.connect(device)
        if(bleService?.manager?.isConnected == false){
            bleService?.bleManager?.log(Log.VERBOSE, " couldn't connect to BT device")
        }

        presetService = bleService!!.manager.presetService
        triggerService = bleService!!.manager.triggerService
        battery_service = bleService!!.manager.batteryService
    }

    fun disconnect(){
        bleService?.manager?.disconnect()
    }

    fun Hand_activation_by_preset(preset_number: Int){

        //TODO: check if preset_number validations in trigger function are sufficient
        try {
            triggerService?.trigger(preset_number)
        }
        catch (e: Exception){
            print("execption was thrown")
        }

    }

     //suspend fun Extract_preset_anotations(): MutableList<HandAction?>{
     //   val presets_list = mutableListOf<HandAction?>()
     //   //TODO: check presets range and check "?" deal
      //  for (i in 0..11) {
      //      presets_list.add(presetService?.readPreset(i))
       // }
       // return presets_list
    //}

    //fun Extract_battery_status(){


    //}
}
