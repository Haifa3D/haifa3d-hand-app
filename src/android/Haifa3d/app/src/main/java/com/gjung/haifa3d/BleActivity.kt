package com.gjung.haifa3d

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.haifa3d_ble_api.BleAPICommands
import com.example.haifa3d_ble_api.ble.BleService


abstract class BleActivity : AppCompatActivity() {
    protected var bleService: BleService? = null
    protected var API_obj: BleAPICommands = BleAPICommands()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            //val binder = service as BleService.LocalBinder
            bleService = API_obj.Get_ble_service()
            onServiceConnected()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
            onServiceDisconnected()
            finish()
        }
    }

    abstract fun onServiceConnected()
    abstract fun onServiceDisconnected()

    abstract fun get_ble_service(): BleService?

    override fun onStart() {
        super.onStart()

        // this line makes it a started service so that it continues to be alive
        // when the app is closed
        startService(Intent(this, BleService::class.java))

        Intent(this, BleService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_IMPORTANT)
        }
    }

    override fun onStop() {
        unbindService(connection)
        bleService = null
        super.onStop()
    }
}