package com.haifa3D.haifa3d_ble_api.model

enum class MotorDirection(val isBitSet: Boolean) {
    Dir1(false),
    Dir2(true)
}

fun Boolean.toMotorDirection() : MotorDirection =
    if (this) MotorDirection.Dir2 else MotorDirection.Dir1