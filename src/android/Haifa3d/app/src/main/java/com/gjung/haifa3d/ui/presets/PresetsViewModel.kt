package com.gjung.haifa3d.ui.presets

import androidx.lifecycle.*
import com.gjung.haifa3d.data.PresetRepository
import com.gjung.haifa3d.model.HandAction
import com.gjung.haifa3d.model.Preset
import com.gjung.haifa3d.notifyObserver
import kotlinx.coroutines.launch

class PresetsViewModel internal constructor(
    private val presetRepository: PresetRepository
) : ViewModel() {
    val presets = MutableLiveData<MutableList<Preset>>().apply {
        value = mutableListOf()
    }

    val connectedHandDeviceAddress = MutableLiveData<String>()

    // see https://stackoverflow.com/a/57819928/1200847
    val presetNames = Transformations.switchMap(presets) { newPresets ->
        Transformations.switchMap(connectedHandDeviceAddress) { newAddress ->
            Transformations.map(presetRepository.getHandDevicePresets(newAddress)) { dbPresets ->
                newPresets.mapNotNull {
                    key ->
                        val value = (dbPresets.firstOrNull { it.blePresetId == key.id })?.name
                        if(value == null) null else key to value
                }.toMap()
            }
        }
    }

    suspend fun setPresetName(presetId: Int, content: HandAction, name: String?) {
        presetRepository.saveHandDevicePreset(
            connectedHandDeviceAddress.value!!, name, presetId, content)
    }
}