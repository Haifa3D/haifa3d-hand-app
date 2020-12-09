package com.gjung.haifa3d.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
        binding.viewGithub.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url)))
            startActivity(browserIntent)
        }
        setActivityTitle("About")

        return binding.root
    }

    fun Fragment.setActivityTitle(title: String)
    {
        (activity as AppCompatActivity?)!!.supportActionBar?.title = title
    }


}
