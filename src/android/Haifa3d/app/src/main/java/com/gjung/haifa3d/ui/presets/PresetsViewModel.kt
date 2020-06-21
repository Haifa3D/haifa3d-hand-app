package com.gjung.haifa3d.ui.presets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gjung.haifa3d.model.Preset

class PresetsViewModel : ViewModel() {
    val presets = MutableLiveData<MutableList<Preset>>().apply {
        value = mutableListOf()
    }
}