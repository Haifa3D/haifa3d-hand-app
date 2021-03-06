package com.gjung.haifa3d.ui.speaktoaction

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.gjung.haifa3d.BleFragment
import com.gjung.haifa3d.adapter.PresetsAdapter
import com.gjung.haifa3d.databinding.FragmentSpeakToActionBinding
import com.haifa3D.haifa3d_ble_api.ble.IPresetService
import com.haifa3D.haifa3d_ble_api.ble.ITriggerService
import com.haifa3D.haifa3d_ble_api.model.HandAction
import kotlinx.android.synthetic.main.fragment_speak_to_action.*
import java.util.*


class SpeakToActionFragment :BleFragment() {

    private lateinit var binding: FragmentSpeakToActionBinding
    private var presetsService: IPresetService? = null
    private var triggerService: ITriggerService? = null
    private lateinit var adapter: PresetsAdapter
    private var REQUEST_CODE_SPEECH_INPUT = 100

    override fun onServiceConnected() {

    }

    override fun onServiceDisconnected() {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSpeakToActionBinding.inflate(layoutInflater, container, false)
        setActivityTitle("Speak To Action")
        val mSpannableText = SpannableString( (activity as AppCompatActivity?)!!.supportActionBar?.title)
        mSpannableText.setSpan(
            ForegroundColorSpan(Color.WHITE),
            0,
            mSpannableText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        (activity as AppCompatActivity?)!!.supportActionBar?.title = mSpannableText
        // Inflate the layout for this fragment
        binding.voiceBtn.setOnClickListener{speak()}
        return binding.root
    }

    fun Fragment.setActivityTitle(title: String)
    {
        (activity as AppCompatActivity?)!!.supportActionBar?.title = title
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

     private fun speak(){

        val mIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        mIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi speak something")

        try {
            startActivityForResult(mIntent, REQUEST_CODE_SPEECH_INPUT)
        }
        catch (e: Exception){

            //Toast.makeText(this@Conne, e.message, Toast.LENGTH_SHORT).show()
        }

    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CODE_SPEECH_INPUT->{
                if(resultCode == Activity.RESULT_OK && null != data){

                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val list = result[0].split(" ")
                    textView2.text = result[0]
                    if (list.size>=3){
                        if((list[0]=="extract" || list[0]=="Extract") && list[1]=="battery" && list[2]=="status"){
                            val currentPercentage: Int = this.apiObject.Extract_battery_status()
                            textView2.text = "Battery level is: " + currentPercentage + " %"
                        }
                        if((list[0]=="activate" || list[0]=="Activate")  && list[1]=="preset" && list[2]=="number"){
                            if(list[3] != null){
                                var convertedNum = convertNumToint(list[3])
                                this.apiObject.Hand_activation_by_preset(convertedNum)
                            }

                        }


                    }


                }
            }
        }
    }

    private fun convertNumToint(num : String): Int{
        var res = -1
        when (num) {
            "zero" -> res = 0
            "one" -> res = 1
            "two" -> res = 2
            "three" -> res = 3
            "four" -> res = 4
            "five" -> res = 5
            "six" -> res = 6
            "serven" -> res = 7
            "eight" -> res = 8
            "nine" -> res = 9
            "ten" -> res = 10
            "eleven" -> res = 11
            "0" -> res = 0
            "1" -> res = 1
            "2" -> res = 2
            "3" -> res = 3
            "4" -> res = 4
            "5" -> res = 5
            "6" -> res = 6
            "7" -> res = 7
            "8" -> res = 8
            "9" -> res = 9
            "10" -> res = 10
            "11" -> res = 11
        }

            return res
    }



}

