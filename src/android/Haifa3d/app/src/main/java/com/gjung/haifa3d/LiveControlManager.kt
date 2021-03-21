package com.gjung.haifa3d

import android.os.Handler
import com.haifa3D.haifa3d_ble_api.ble.IDirectExecuteService
import com.haifa3D.haifa3d_ble_api.model.*
//import com.gjung.haifa3d.ble.IDirectExecuteService
//import com.gjung.haifa3d.model.*

class LiveControlManager(private val service: IDirectExecuteService, private val handler: Handler) {
    var turnDirection: MotorDirection? = null
    var finger1Direction: MotorDirection? = null
    var finger2Direction: MotorDirection? = null
    var finger3Direction: MotorDirection? = null
    var finger4Direction: MotorDirection? = null

    private val sendInterval: Long = 40

    fun ensureStarted() {
        handler.post{
            handlerCallback()
        }
    }

    fun stopAll() {
        finger1Direction = null
        finger2Direction = null
        finger3Direction = null
        finger4Direction = null
        turnDirection = null
    }

    private val needsLoop
        get() =
            finger1Direction != null ||
            finger2Direction != null ||
            finger3Direction != null ||
            finger4Direction != null ||
            turnDirection != null

    private fun sendAction() {
        val act = HandAction(
            HandMovement(
                TorqueStopModeDetail(TorqueStopThreshold.Low),
                TimeStopModeDetail(1u),
                MotorsActivated(
                    turnDirection != null,
                    finger1Direction != null,
                    finger2Direction != null,
                    finger3Direction != null,
                    finger4Direction != null
                ),
                MotorsDirection(
                    turnDirection ?: MotorDirection.Dir1,
                    finger1Direction ?: MotorDirection.Dir1,
                    finger2Direction ?: MotorDirection.Dir1,
                    finger3Direction ?: MotorDirection.Dir1,
                    finger4Direction ?: MotorDirection.Dir1
                )
            )
        )
        service.executeAction(act)
    }

    private fun handlerCallback() {
        sendAction()
        if (needsLoop)
            handler.postDelayed({ handlerCallback() }, sendInterval)
    }
}