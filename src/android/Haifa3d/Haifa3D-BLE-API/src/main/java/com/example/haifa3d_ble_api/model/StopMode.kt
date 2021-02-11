package com.example.haifa3d_ble_api.model

enum class StopMode(val isBitSet: Boolean) {
    Torque(false),
    Time(true)
}