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


abstract class BleActivity : AppCompatActivity(), BleAPICommands.IBleListener {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiObject.bind(this,this,Intent(this, BleService::class.java))

    }

    override fun onStart() {
        super.onStart()
        //Api_obj.bind(this,this,Intent(this, BleService::class.java))

        // this line makes it a started service so that it continues to be alive
        // when the app is closed
        //startService()
        //Intent(this, BleService::class.java).also { intent ->
       //     bindService(intent, c, Context.BIND_IMPORTANT)
       // }
    }

    override fun onStop() {
        apiObject.unbind(this)
        //unbindService(connection)
        //bleService = null
        super.onStop()
    }
}