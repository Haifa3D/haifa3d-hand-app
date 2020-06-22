package com.gjung.haifa3d.ui.about

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gjung.haifa3d.BuildConfig

import com.gjung.haifa3d.R
import com.gjung.haifa3d.databinding.FragmentAboutBinding

/**
 * A simple [Fragment] subclass.
 */
class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAboutBinding.inflate(layoutInflater, container, false)

        binding.versionLabel.text = getString(R.string.version_label, BuildConfig.VERSION_NAME)
        binding.viewOssLicensesButton.setOnClickListener {
            val act = AboutFragmentDirections.showLicenseInfo()
            this@AboutFragment.findNavController().navigate(act)
        }

        return binding.root
    }

}
