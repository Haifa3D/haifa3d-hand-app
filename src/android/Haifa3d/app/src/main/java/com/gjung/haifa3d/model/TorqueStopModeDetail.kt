package com.gjung.haifa3d.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.gjung.haifa3d.toBits
import com.gjung.haifa3d.toByte
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class TorqueStopModeDetail(
    val turn: TorqueStopThreshold,
    val finger1: TorqueStopThreshold,
    val finger2: TorqueStopThreshold,
    val finger3: TorqueStopThreshold,
    val finger4: TorqueStopThreshold
): ByteRepresentable, Parcelable {

    constructor(all: TorqueStopThreshold) : this(all, all, all, all, all)

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

fun Byte.decodeTorqueStopModeDetail(): TorqueStopModeDetail {
    val bits = this.toBits()
    return TorqueStopModeDetail(
        bits[0].toTorqueStopThreshold(),
        bits[1].toTorqueStopThreshold(),
        bits[2].toTorqueStopThreshold(),
        bits[3].toTorqueStopThreshold(),
        bits[4].toTorqueStopThreshold()
    )
}