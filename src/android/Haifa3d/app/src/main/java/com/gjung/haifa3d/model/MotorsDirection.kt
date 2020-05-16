package com.gjung.haifa3d.model

import com.gjung.haifa3d.toByte

class MotorsDirection(val turn: MotorDirection,
                      val finger1: MotorDirection,
                      val finger2: MotorDirection,
                      val finger3: MotorDirection,
                      val finger4: MotorDirection
): ByteRepresentable {

        override fun toBytes(): Iterable<Byte> =
        listOf(
            listOf(
                turn.isBitSet,
                finger1.isBitSet,
                finger2.isBitSet,
                finger3.isBitSet,
                finger4.isBitSet)
                .toByte()
        )
}