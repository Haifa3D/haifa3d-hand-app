package com.gjung.haifa3d.ui.presets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gjung.haifa3d.BleFragment

import com.gjung.haifa3d.R
import com.gjung.haifa3d.adapter.PresetsAdapter
import com.gjung.haifa3d.ble.IPresetService
import com.gjung.haifa3d.ble.ITriggerService
import com.gjung.haifa3d.databinding.FragmentPresetsBinding
import com.gjung.haifa3d.model.HandAction
import com.gjung.haifa3d.model.Preset
import com.gjung.haifa3d.notifyObserver
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 */
class PresetsFragment : BleFragment() {
    private lateinit var binding: FragmentPresetsBinding
    private var presetService: IPresetService? = null
    private var triggerService: ITriggerService? = null
    private lateinit var adapter: PresetsAdapter
    private val presetsViewModel: PresetsViewModel by activityViewModels()

    override fun onServiceConnected() {
        presetService = bleService!!.manager.presetService
        triggerService  = bleService!!.manager.triggerService

        val presets = presetsViewModel.presets.value!!
        presets.clear()
        for(i in 0..11) {
            presets.add(Preset(i))
        }
        presetsViewModel.presets.value = presets
    }

    override fun onServiceDisconnected() {
        presetService = null
        triggerService = null

        presetsViewModel.presets.value!!.clear()
        presetsViewModel.presets.notifyObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPresetsBinding.inflate(layoutInflater, container, false)

        val rec = binding.recyclerViewPresets

        adapter = PresetsAdapter(presetsViewModel)
        adapter.onItemClickListener = object : PresetsAdapter.OnItemClickListener {
            override fun onItemClick(preset: Preset) {
                if (preset.handAction == null) {
                    GlobalScope.launch(Dispatchers.Main) {
                        try {
                            preset.handAction = presetService?.readPreset(preset.id)
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

        adapter.onItemEditClickListener = object : PresetsAdapter.OnItemClickListener {
            override fun onItemClick(preset: Preset) {
                GlobalScope.launch(Dispatchers.Main) {
                    // ensure preset is loaded or use empty if not set
                    try {
                        preset.handAction = presetService?.readPreset(preset.id)
                    } catch(ex: Throwable) {
                        preset.handAction = HandAction.Empty
                    }
                    val act = PresetsFragmentDirections.editPreset(preset.id)
                    this@PresetsFragment.findNavController().navigate(act)
                }
            }
        }

        presetsViewModel.presets.observe(viewLifecycleOwner, Observer {
            adapter.notifyDataSetChanged()
        })

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
