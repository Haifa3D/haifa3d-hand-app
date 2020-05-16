package com.gjung.haifa3d.model

enum class StopMode(val isBitSet: Boolean) {
    Torque(false),
    Time(true)
}