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

        // this line makes it a started service so that it continues to be alive
        // when the app is closed
        //startService(Intent(this, BleService::class.java))
        var intent: Intent = Intent(this, BleService::class.java)
        Api_obj.bind(this,this,intent)
        //Intent(this, BleService::class.java).also { intent ->
        //    bindService(intent, connection, Context.BIND_IMPORTANT)
        //}
    }

    override fun onStop() {
        Api_obj.unbind(this)
        //unbindService(connection)
        //bleService = null
        super.onStop()
    }
}