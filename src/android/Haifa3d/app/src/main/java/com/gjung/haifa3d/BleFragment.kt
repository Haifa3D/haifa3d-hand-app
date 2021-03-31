package com.gjung.haifa3d

import android.content.Intent
import androidx.fragment.app.Fragment
import com.haifa3D.haifa3d_ble_api.ble.BleService
import com.haifa3D.haifa3d_ble_api.BleAPICommands
import com.haifa3D.haifa3d_ble_api.BleAPICommands.IBleListener

abstract class BleFragment : Fragment(),IBleListener{
    protected var bleService: BleService? = null
    protected var apiObject = BleAPICommands()

    override fun onConnected(bleService: BleService) {
        this.bleService = bleService
        onServiceConnected()

    }

    override fun onDisconnected() {
        bleService = null
        onServiceDisconnected()
    }

    abstract fun onServiceConnected()
    abstract fun onServiceDisconnected()


    override fun onStart() {
        super.onStart()
        var intent: Intent = Intent(requireContext(), BleService::class.java)
        intent.setType("fragment")
        apiObject.bind(this,requireContext(),intent)

    }

    override fun onStop() {
        apiObject.unbind(requireContext())

        super.onStop()
    }
}