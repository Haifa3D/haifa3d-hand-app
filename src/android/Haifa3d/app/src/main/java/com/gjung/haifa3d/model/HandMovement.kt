package com.gjung.haifa3d.model

class HandMovement() : ByteRepresentable {
    private lateinit var repr: List<Byte>

    constructor(
        torqueDetail: TorqueStopModeDetail,
        timeDetail: TimeStopModeDetail,
        motorsActivated: MotorsActivated,
        motorsDirection: MotorsDirection
    ) : this() {
        repr = listOf(torqueDetail, timeDetail, motorsActivated, motorsDirection).flatMap { it.toBytes() }
    }

    override fun toBytes(): Iterable<Byte> = repr
}