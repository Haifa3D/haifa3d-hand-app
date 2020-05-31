package com.gjung.haifa3d.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gjung.haifa3d.BleFragment
import com.gjung.haifa3d.R
import com.gjung.haifa3d.databinding.FragmentHomeBinding
import com.gjung.haifa3d.model.*

class HomeFragment : BleFragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding

    override fun onServiceConnected() {
    }

    override fun onServiceDisconnected() {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        binding.executeDirectlyTorque.setOnClickListener {
            bleService!!.manager.directExecuteService.executeAction(
                HandAction(
                    HandMovement(
                        TorqueStopModeDetail(TorqueStopThreshold.High),
                        TimeStopModeDetail(139.toByte()),
                        MotorsActivated(
                            turn = true,
                            finger1 = true,
                            finger2 = false,
                            finger3 = false,
                            finger4 = true
                        ),
                        MotorsDirection(MotorDirection.Dir1, MotorDirection.Dir1, MotorDirection.Dir2, MotorDirection.Dir1, MotorDirection.Dir1)
                    )
                )
            )
        }

        binding.executeDirectlyTime.setOnClickListener {
            bleService!!.manager.directExecuteService.executeAction(
                HandAction(
                    HandMovement(
                        TorqueStopModeDetail(TorqueStopThreshold.Low),
                        TimeStopModeDetail(255.toByte()),
                        MotorsActivated(
                            turn = false,
                            finger1 = true,
                            finger2 = false,
                            finger3 = false,
                            finger4 = false
                        ),
                        MotorsDirection(MotorDirection.Dir1, MotorDirection.Dir1, MotorDirection.Dir1, MotorDirection.Dir1, MotorDirection.Dir1)
                    ),
                    HandMovement(
                        TorqueStopModeDetail(TorqueStopThreshold.Low),
                        TimeStopModeDetail(255.toByte()),
                        MotorsActivated(
                            turn = false,
                            finger1 = true,
                            finger2 = false,
                            finger3 = false,
                            finger4 = false
                        ),
                        MotorsDirection(MotorDirection.Dir1, MotorDirection.Dir2, MotorDirection.Dir1, MotorDirection.Dir1, MotorDirection.Dir1)
                    )
                )
            )
        }

        return binding.root
    }
}
