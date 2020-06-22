package com.gjung.haifa3d.ui.config

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gjung.haifa3d.BleFragment

import com.gjung.haifa3d.R
import com.gjung.haifa3d.databinding.FragmentConfigurationBinding

class ConfigurationFragment : BleFragment() {

    private lateinit var binding: FragmentConfigurationBinding

    override fun onServiceConnected() {
    }

    override fun onServiceDisconnected() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConfigurationBinding.inflate(layoutInflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }
}
