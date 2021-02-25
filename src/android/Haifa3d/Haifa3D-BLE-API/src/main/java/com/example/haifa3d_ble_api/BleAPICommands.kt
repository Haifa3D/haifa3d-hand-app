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


    interface BleListener { // THINK ABOUT MOVE INTERFACE DECLERATION TO LIBRARY
        fun onServiceConnected(bleService: BleService)
        fun onServiceDisconnected()
    }


    fun bind(callback:BleListener): ServiceConnection{
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


    fun connect(device: BluetoothDevice){

        bleService.manager.connect(device)
        if(bleService.manager.isConnected == false){
            bleService.bleManager.log(Log.VERBOSE, " couldn't connect to BT device")
        }

        presetService = bleService!!.manager.presetService
        triggerService = bleService!!.manager.triggerService
        battery_service = bleService!!.manager.batteryService
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
