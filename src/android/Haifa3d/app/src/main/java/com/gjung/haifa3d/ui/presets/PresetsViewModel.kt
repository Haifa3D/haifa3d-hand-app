package com.gjung.haifa3d.ui.presets

import androidx.lifecycle.*
import com.gjung.haifa3d.data.PresetRepository
import com.example.haifa3d_ble_api.model.HandAction
import com.example.haifa3d_ble_api.model.Preset
//import com.gjung.haifa3d.model.HandAction
//import com.gjung.haifa3d.model.Preset
import com.gjung.haifa3d.notifyObserver
import kotlinx.coroutines.launch

class PresetsViewModel internal constructor(
    private val presetRepository: PresetRepository
) : ViewModel() {
    val presets = MutableLiveData<MutableList<Preset>>().apply {
        value = mutableListOf()
    }

    val connectedHandDeviceAddress = MutableLiveData<String>()

    private val dbPresets = Transformations.switchMap(presets) { _ ->
        Transformations.switchMap(connectedHandDeviceAddress) { newAddress ->
            presetRepository.getHandDevicePresets(newAddress)
        }
    }

    // see https://stackoverflow.com/a/57819928/1200847
    val presetNames = Transformations.map(dbPresets) { dbPresets ->
        presets.value!!.mapNotNull {
            key ->
                val value = (dbPresets.firstOrNull { it.blePresetId == key.id })?.name
                if(value == null) null else key to value
        }.toMap()
    }

    val starredPresets = Transformations.map(dbPresets) { dbPresets ->
        presets.value!!.filter {
            preset ->
                (dbPresets.firstOrNull { it.blePresetId == preset.id }?.starred ?: false)
        }
    }

    val currentEditPresetStarred = MutableLiveData<Boolean>()
    val currentEditPresetName = MutableLiveData<String>()

    suspend fun setPresetInfo(presetId: Int, content: HandAction, name: String?, isStarred: Boolean) {
        presetRepository.saveHandDevicePreset(
            connectedHandDeviceAddress.value!!, name, presetId, content, isStarred)
    }
}