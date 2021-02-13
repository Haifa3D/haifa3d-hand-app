package com.gjung.haifa3d

import android.content.Intent

class MainActivity: BleActivity() {


    override fun onServiceConnected() {
        lateinit var intent: Intent
        if (bleService!!.manager.isConnected) {
            intent = Intent(this, ConnectedActivity::class.java)
        } else {
            intent = Intent(this, ScannerActivity::class.java)
        }

        startActivity(intent)
        finish()
    }

    override fun onServiceDisconnected() {
        finish()
    }
}