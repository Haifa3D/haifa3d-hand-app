package com.example.haifa3d_ble_api

import android.content.Intent
import com.example.haifa3d_ble_api.ble.IPresetService
import com.example.haifa3d_ble_api.ble.ITriggerService
import com.example.haifa3d_ble_api.ble.IBatteryLevelService
import com.example.haifa3d_ble_api.model.HandAction


class BleAPICommands () {
    private var presetService: IPresetService? = null
    private var triggerService: ITriggerService? = null
    private var battery_service: IBatteryLevelService? = null

    fun Hand_activation_by_preset(preset_number: Int){

        //TODO: check if preset_number validations in trigger function are sufficient
        triggerService?.trigger(preset_number)
    }

     suspend fun Extract_preset_anotations(): MutableList<HandAction?>{
        val presets_list = mutableListOf<HandAction?>()
        //TODO: check presets range and check "?" deal
        for (i in 0..11) {
            presets_list.add(presetService?.readPreset(i))
        }
        return presets_list
    }

    fun Extract_battery_status(){


    }




}