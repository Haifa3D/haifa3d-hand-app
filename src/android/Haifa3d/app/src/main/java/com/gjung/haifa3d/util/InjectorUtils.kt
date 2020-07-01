package com.gjung.haifa3d.util

import android.content.Context
import com.gjung.haifa3d.data.AppDatabase
import com.gjung.haifa3d.data.PresetRepository
import com.gjung.haifa3d.ui.presets.PresetsViewModelFactory

object InjectorUtils {
    private fun getPresetRepository(context: Context): PresetRepository {
        val appDb = AppDatabase.getInstance(context)
        return PresetRepository.getInstance(appDb.presetDao(), appDb.handDeviceDao())
    }

    fun providePresetsViewModelFactory(context: Context) =
        PresetsViewModelFactory(getPresetRepository(context))
}