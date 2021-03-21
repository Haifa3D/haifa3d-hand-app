package com.gjung.haifa3d.ui.livecontrol

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

import com.gjung.haifa3d.databinding.FragmentLiveControlBinding
import com.haifa3D.haifa3d_ble_api.model.MotorDirection
//import com.gjung.haifa3d.model.MotorDirection

class LiveControlFragment : BleFragment() {
    private lateinit var binding: FragmentLiveControlBinding
    private var liveControl: LiveControlManager? = null
    private lateinit var liveControlHandler: Handler

    override fun onServiceConnected() {
        liveControl = LiveControlManager(bleService!!.manager.directExecuteService, liveControlHandler)
    }

    override fun onServiceDisconnected() {
        liveControl!!.stopAll()
        liveControl = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        binding = FragmentLiveControlBinding.inflate(layoutInflater, container, false)

        binding.finger1OpenButton.setDirectionWhilePressed({ liveControl?.finger1Direction = it; }, MotorDirection.Dir1)
        binding.finger2OpenButton.setDirectionWhilePressed({ liveControl?.finger2Direction = it; }, MotorDirection.Dir1)
        binding.finger3OpenButton.setDirectionWhilePressed({ liveControl?.finger3Direction = it; }, MotorDirection.Dir1)
        binding.finger4OpenButton.setDirectionWhilePressed({ liveControl?.finger4Direction = it; }, MotorDirection.Dir1)
        binding.finger1CloseButton.setDirectionWhilePressed({ liveControl?.finger1Direction = it; }, MotorDirection.Dir2)
        binding.finger2CloseButton.setDirectionWhilePressed({ liveControl?.finger2Direction = it; }, MotorDirection.Dir2)
        binding.finger3CloseButton.setDirectionWhilePressed({ liveControl?.finger3Direction = it; }, MotorDirection.Dir2)
        binding.finger4CloseButton.setDirectionWhilePressed({ liveControl?.finger4Direction = it; }, MotorDirection.Dir2)
        binding.turnRightButton.setDirectionWhilePressed({ liveControl?.turnDirection = it; }, MotorDirection.Dir1)
        binding.turnLeftButton.setDirectionWhilePressed({ liveControl?.turnDirection = it; }, MotorDirection.Dir2)

        liveControlHandler = Handler(Looper.getMainLooper())

        return binding.root
    }

    private fun View.setDirectionWhilePressed( setter: (MotorDirection?) -> Unit, setWhilePressed: MotorDirection) {
        this.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                this.isPressed = true
                setter(setWhilePressed)
                liveControl?.ensureStarted()
            }
            else if (event.action == MotionEvent.ACTION_UP) {
                setter(null)
                this.isPressed = false
            }
            true
        }
    }

    fun Fragment.setActivityTitle(title: String)
    {
        (activity as AppCompatActivity?)!!.supportActionBar?.title = title
    }

}
