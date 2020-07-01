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
import com.gjung.haifa3d.ble.BleService

abstract class BleFragment : Fragment() {
    protected var bleService: BleService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BleService.LocalBinder
            bleService = binder.getService()
            onServiceConnected()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
            onServiceDisconnected()
        }
    }

    abstract fun onServiceConnected()
    abstract fun onServiceDisconnected()

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