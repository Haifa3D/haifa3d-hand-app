package com.gjung.haifa3d.profile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gjung.haifa3d.model.HandAction


class AppBleService: BatteryService() {
    val EXTRA_EXECUTE_ACTION = "com.gjung.haifa3d.EXTRA_EXECUTE_ACTION"

    private val mgr: AppBleManager = AppBleManager(this)

    private val directExecuteReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.getParcelableExtra<HandAction>(EXTRA_EXECUTE_ACTION) ?: return
                mgr.executeAction(action)
            }
        }

    override fun initializeManager(): AppBleManager {
        mgr.batteryObserver = this
        return mgr
    }
}