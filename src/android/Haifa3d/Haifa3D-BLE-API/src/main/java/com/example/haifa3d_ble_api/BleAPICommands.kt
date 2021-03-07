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


    interface IBleListener { // THINK ABOUT MOVING INTERFACE DECLERATION TO LIBRARY
        fun onServiceConnected(bleService: BleService)
        fun onServiceDisconnected()
    }

    // here we bind to the android service and return an instance of ServiceConnection // do we need to pass an interface object that implements onSerivce methods
    fun bind(callback:IBleListener): ServiceConnection{
         val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as BleService.LocalBinder
                bleService = binder.getService()
                callback.onServiceConnected(bleService)

            }

            override fun onServiceDisconnected(name: ComponentName?) {
                bleService = null
                callback.onServiceDisconnected()
            }
        }
    }

    //here we connect the service(after binding) with a specific device
    fun connect(device: BluetoothDevice){

        bleService.manager.connect(device)
        if(bleService.manager.isConnected == false){
            bleService.bleManager.log(Log.VERBOSE, " couldn't connect to BT device")
        }

        presetService = bleService!!.manager.presetService
        triggerService = bleService!!.manager.triggerService
        battery_service = bleService!!.manager.batteryService
    }

    // is it the right way to get an instance of BleService
    fun get_bleService_instance(): BleService
    {

    }

    fun disconnect(){
        bleService.manager.disconnect()
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

    fun Get_ble_service(): BleService{
        return this.bleService
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
