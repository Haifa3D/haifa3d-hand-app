package com.gjung.haifa3d.ui.status

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gjung.haifa3d.BleFragment
import com.gjung.haifa3d.ble.BatteryObserver
import com.gjung.haifa3d.databinding.FragmentStatusBinding

class StatusFragment : BleFragment() {

    private val statusViewModel: StatusViewModel by viewModels()
    private lateinit var binding: FragmentStatusBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatusBinding.inflate(layoutInflater, container, false)

        statusViewModel.batteryPercentage.observe(viewLifecycleOwner, Observer {
            binding.batteryPercentage.text = "Battery Percentage: $it %"
        })

        return binding.root
    }

    override fun onServiceConnected() {
        statusViewModel.batteryPercentage.value = bleService!!.manager.batteryService.batteryLevel
        bleService!!.manager.batteryService.observer = object : BatteryObserver {
            override fun onBatteryValueReceived(device: BluetoothDevice, percentage: Int) {
                statusViewModel.batteryPercentage.value = percentage
            }
        }
    }

    override fun onServiceDisconnected() {
    }
}
