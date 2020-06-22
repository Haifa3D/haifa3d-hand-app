package com.gjung.haifa3d.ui.status

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gjung.haifa3d.BleFragment
import com.gjung.haifa3d.databinding.FragmentStatusBinding

class StatusFragment : BleFragment() {

    private lateinit var binding: FragmentStatusBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatusBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onServiceConnected() {
        bleService!!.manager.batteryService.currentPercentage.observe(viewLifecycleOwner, Observer {
            binding.batteryPercentage.text = "Battery Percentage: ${it?.percentage ?: "?"} %"
        })
    }

    override fun onServiceDisconnected() {
    }
}
