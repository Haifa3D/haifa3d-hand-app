package com.gjung.haifa3d.model

class TimeStopModeDetail(val durationTimeUnitCount: Byte): ByteRepresentable {
    override fun toBytes(): Iterable<Byte> =
        listOf(durationTimeUnitCount)
}