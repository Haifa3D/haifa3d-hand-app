package com.example.haifa3d_ble_api.model

enum class TorqueStopThreshold(val isBitSet: Boolean) {
    Low(false),
    High(true)
}

fun Boolean.toTorqueStopThreshold() : TorqueStopThreshold =
    if (this) TorqueStopThreshold.High else TorqueStopThreshold.Low