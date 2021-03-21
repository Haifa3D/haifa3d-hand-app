package com.haifa3D.haifa3d_ble_api.model

enum class StopMode(val isBitSet: Boolean) {
    Torque(false),
    Time(true)
}