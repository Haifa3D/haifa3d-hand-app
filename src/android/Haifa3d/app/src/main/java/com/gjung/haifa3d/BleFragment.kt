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

abstract class BleFragment : Fragment() {
    protected var bleService: BleService? = null
    lateinit var callback: BleListener
    protected lateinit var callback_object: BleListener
    protected var Api_obj: BleAPICommands = BleAPICommands()
    private lateinit var connection: ServiceConnection

    init {
        callback_object = BleListener()
        connection = Api_obj.bind(callback)
    }


    abstract fun onServiceConnected()
    abstract fun onServiceDisconnected()


    class BleListener(): IBleListener {
        override fun onServiceConnected(bleService: BleService) {
            //val binder = service as BleService.LocalBinder
            //bleService = binder.getService()
        }

        override fun onServiceDisconnected() {
            //bleService = null
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(requireContext(), BleService::class.java).also { intent ->
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT)
        }
    }

    override fun onStop() {
        requireContext().unbindService(connection)
        bleService = null
        super.onStop()
    }
}