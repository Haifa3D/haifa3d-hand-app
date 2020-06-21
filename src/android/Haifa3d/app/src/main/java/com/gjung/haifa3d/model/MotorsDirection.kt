package com.gjung.haifa3d.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.gjung.haifa3d.toBits
import com.gjung.haifa3d.toByte
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class MotorsDirection(val turn: MotorDirection = MotorDirection.Dir1,
                      val finger1: MotorDirection = MotorDirection.Dir1,
                      val finger2: MotorDirection = MotorDirection.Dir1,
                      val finger3: MotorDirection = MotorDirection.Dir1,
                      val finger4: MotorDirection = MotorDirection.Dir1
): ByteRepresentable, Parcelable {

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

fun Byte.decodeMotorsDirection(): MotorsDirection {
    val bits = this.toBits()
    return MotorsDirection(
        bits[0].toMotorDirection(),
        bits[1].toMotorDirection(),
        bits[2].toMotorDirection(),
        bits[3].toMotorDirection(),
        bits[4].toMotorDirection()
    )
}