package com.gjung.haifa3d.ui.presets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gjung.haifa3d.data.PresetRepository

class PresetsViewModelFactory(
    private val repository: PresetRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        PresetsViewModel(repository) as T
}