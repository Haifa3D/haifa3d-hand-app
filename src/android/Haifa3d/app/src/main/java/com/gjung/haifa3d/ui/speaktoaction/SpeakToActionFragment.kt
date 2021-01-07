package com.gjung.haifa3d.ui.speaktoaction
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.gjung.haifa3d.BleFragment
import com.gjung.haifa3d.LiveControlManager
import com.gjung.haifa3d.R
import com.gjung.haifa3d.adapter.PresetsAdapter
import com.gjung.haifa3d.ble.IPresetService
import com.gjung.haifa3d.databinding.FragmentVoiceControlBinding
import com.gjung.haifa3d.model.MotorDirection

class SpeakToActionFragment :BleFragment() {

    private lateinit var binding: FragmentVoiceControlBinding
    private var presetsService: IPresetService? = null
    private lateinit var adapter: PresetsAdapter


    override fun onServiceConnected() {
        TODO("Not yet implemented")
    }

    override fun onServiceDisconnected() {
        presetsService = null
        //TODO: change this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVoiceControlBinding.inflate(layoutInflater, container, false)
        setActivityTitle("Live Control")
        val mSpannableText = SpannableString( (activity as AppCompatActivity?)!!.supportActionBar?.title)
        mSpannableText.setSpan(
            ForegroundColorSpan(Color.WHITE),
            0,
            mSpannableText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        (activity as AppCompatActivity?)!!.supportActionBar?.title = mSpannableText
        // Inflate the layout for this fragment

        return binding.root
    }

    fun Fragment.setActivityTitle(title: String)
    {
        (activity as AppCompatActivity?)!!.supportActionBar?.title = title
    }

}

