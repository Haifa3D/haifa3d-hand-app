package com.gjung.haifa3d.ui.presets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gjung.haifa3d.BleFragment

import com.gjung.haifa3d.R
import com.gjung.haifa3d.adapter.PresetsAdapter
import com.gjung.haifa3d.ble.PresetService
import com.gjung.haifa3d.ble.TriggerService
import com.gjung.haifa3d.databinding.FragmentPresetsBinding
import com.gjung.haifa3d.model.HandAction
import com.gjung.haifa3d.model.Preset
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 */
class PresetsFragment : BleFragment() {
    private lateinit var binding: FragmentPresetsBinding
    private var presets: PresetService? = null
    private var triggerService: TriggerService? = null
    private lateinit var adapter: PresetsAdapter

    override fun onServiceConnected() {
        presets = bleService!!.manager.presetService
        triggerService  = bleService!!.manager.triggerService
        for(i in 0..11) {
            adapter.presets.add(Preset(i))
        }
        adapter.notifyDataSetChanged()
    }

    override fun onServiceDisconnected() {
        presets = null
        triggerService = null

        adapter.presets.clear()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPresetsBinding.inflate(layoutInflater, container, false)

        val rec = binding.recyclerViewPresets

        adapter = PresetsAdapter()
        adapter.onItemClickListener = object : PresetsAdapter.OnItemClickListener {
            override fun onItemClick(preset: Preset) {
                if (preset.handAction == null) {
                    GlobalScope.launch(Dispatchers.Main) {
                        try {
                            preset.handAction = presets?.readPreset(preset.id)
                            triggerService?.trigger(preset.id)
                        } catch(ex: Throwable) {
                            showSnackbar(R.string.preset_not_set)
                        }
                    }
                } else {
                    triggerService?.trigger(preset.id)
                }
            }
        }
        rec.adapter = adapter
        rec.setHasFixedSize(true)

        rec.layoutManager = LinearLayoutManager(this.requireContext())
        rec.addItemDecoration(DividerItemDecoration(this.requireContext(), DividerItemDecoration.VERTICAL))

        return binding.root
    }

    private fun showSnackbar(@StringRes resId: Int) {
        Snackbar.make(binding.root, resId, Snackbar.LENGTH_LONG)
            .show()
    }

}
