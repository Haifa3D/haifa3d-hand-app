package com.gjung.haifa3d.ui.home

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gjung.haifa3d.BleFragment
import com.gjung.haifa3d.R
import com.gjung.haifa3d.adapter.HomeAdapter
import com.gjung.haifa3d.adapter.PresetsAdapter
import com.gjung.haifa3d.ble.IPresetService
import com.gjung.haifa3d.ble.ITriggerService
import com.gjung.haifa3d.databinding.FragmentHomeBinding
import com.gjung.haifa3d.model.*
import com.gjung.haifa3d.notifyObserver
import com.gjung.haifa3d.ui.presets.PresetsViewModel
import com.gjung.haifa3d.util.InjectorUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeFragment : BleFragment() {

    private var presetService: IPresetService? = null
    private var triggerService: ITriggerService? = null
    private lateinit var adapter: HomeAdapter
    private lateinit var binding: FragmentHomeBinding

    private val presetsViewModel: PresetsViewModel by activityViewModels {
        InjectorUtils.providePresetsViewModelFactory(requireContext())
    }

    override fun onServiceConnected() {
        presetService = bleService!!.manager.presetService
        triggerService  = bleService!!.manager.triggerService

        val presets = presetsViewModel.presets.value!!
        presets.clear()
        for(i in 0..11) {
            presets.add(Preset(i))
        }
        presetsViewModel.presets.value = presets
        presetsViewModel.connectedHandDeviceAddress.value = bleService!!.manager.connectedAddress!!
    }

    override fun onServiceDisconnected() {
        presetService = null
        triggerService = null

        presetsViewModel.presets.value!!.clear()
        presetsViewModel.presets.notifyObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        setActivityTitle("Home")
        val mSpannableText = SpannableString( (activity as AppCompatActivity?)!!.supportActionBar?.title)
        mSpannableText.setSpan(
            ForegroundColorSpan(Color.WHITE),
            0,
            mSpannableText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        (activity as AppCompatActivity?)!!.supportActionBar?.title = mSpannableText

        val rec = binding.recyclerViewPresets

        adapter = HomeAdapter(listOf(), mapOf())
        adapter.onItemClickListener = object : HomeAdapter.OnItemClickListener {
            override fun onItemClick(preset: Preset) {
                if (preset.handAction == null) {
                    GlobalScope.launch(Dispatchers.Main) {
                        try {
                            preset.handAction = presetService?.readPreset(preset.id)
                            presetsViewModel.presets.notifyObserver()
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

        presetsViewModel.starredPresets.observe(viewLifecycleOwner, Observer {
            var starred = presetsViewModel.starredPresets.value!!
            adapter.presets = starred
            adapter.notifyDataSetChanged()
            binding.recyclerViewPresets.visibility = if (starred.any()) View.VISIBLE else View.GONE
            binding.noStarredPresets.root.visibility = if (!starred.any()) View.VISIBLE else View.GONE
        })

        presetsViewModel.presetNames.observe(viewLifecycleOwner, Observer {
            adapter.presetNames = presetsViewModel.presetNames.value!!
            adapter.notifyDataSetChanged()
        })

        rec.adapter = adapter
        rec.setHasFixedSize(true)

        rec.layoutManager = LinearLayoutManager(this.requireContext())

        return binding.root
    }

    fun Fragment.setActivityTitle(title: String)
    {
        (activity as AppCompatActivity?)!!.supportActionBar?.title = title
    }

    private fun showSnackbar(@StringRes resId: Int) {
        Snackbar.make(binding.root, resId, Snackbar.LENGTH_LONG)
            .show()
    }
}
