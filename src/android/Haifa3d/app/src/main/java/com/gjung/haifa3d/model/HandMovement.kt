package com.gjung.haifa3d.model

class HandMovement(
    val torqueDetail: TorqueStopModeDetail,
    val timeDetail: TimeStopModeDetail,
    val motorsActivated: MotorsActivated,
    val motorsDirection: MotorsDirection
) : ByteRepresentable {
    override fun toBytes(): Iterable<Byte> = listOf(torqueDetail, timeDetail, motorsActivated, motorsDirection).flatMap { it.toBytes() }
}