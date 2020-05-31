package com.gjung.haifa3d.model

import com.gjung.haifa3d.toBits
import com.gjung.haifa3d.toByte

class MotorsActivated(
    val turn: Boolean,
    val finger1: Boolean,
    val finger2: Boolean,
    val finger3: Boolean,
    val finger4: Boolean
): ByteRepresentable {

    override fun toBytes(): Iterable<Byte> =
        listOf(
            listOf(
                turn,
                finger1,
                finger2,
                finger3,
                finger4)
            .toByte()
        )
}

fun Byte.decodeMotorsActivated(): MotorsActivated {
    val bits = this.toBits()
    return MotorsActivated(
        bits[0],
        bits[1],
        bits[2],
        bits[3],
        bits[4]
    )
}