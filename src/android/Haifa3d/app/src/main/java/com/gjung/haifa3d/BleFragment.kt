package com.gjung.haifa3d

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.haifa3d_ble_api.ble.BleService
import com.example.haifa3d_ble_api.BleAPICommands
import com.example.haifa3d_ble_api.BleAPICommands.IBleListener
import com.gjung.haifa3d.ble.RealHandService

abstract class BleFragment : Fragment(),IBleListener{
    protected var bleService: BleService? = null
    protected var Api_obj: BleAPICommands = BleAPICommands()

    override fun onConnected(bleService: BleService) {
        this.bleService = bleService
    }

    override fun onDisconnected() {
        bleService = null
    }

    abstract fun onServiceConnected()
    abstract fun onServiceDisconnected()


    override fun onStart() {
        super.onStart()
        Api_obj.bind(this,requireContext())
        //Intent(requireContext(), BleService::class.java).also { intent ->
        //    requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT)
        //}
    }

    override fun onStop() {
        Api_obj.unbind(requireContext())
        //requireContext().unbindService(connection)
        //bleService = null
        super.onStop()
    }
}