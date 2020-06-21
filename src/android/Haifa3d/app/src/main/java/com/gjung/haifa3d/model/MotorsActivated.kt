package com.gjung.haifa3d.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.gjung.haifa3d.toBits
import com.gjung.haifa3d.toByte
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class MotorsActivated(
    val turn: Boolean = false,
    val finger1: Boolean = false,
    val finger2: Boolean = false,
    val finger3: Boolean = false,
    val finger4: Boolean = false
): ByteRepresentable, Parcelable {

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