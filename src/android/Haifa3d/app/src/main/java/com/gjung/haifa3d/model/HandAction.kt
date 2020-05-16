package com.gjung.haifa3d.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class HandAction() : Parcelable, ByteRepresentable {
    lateinit var repr: List<Byte>

    constructor(
        torqueDetail: TorqueStopModeDetail,
        motorsActivated: MotorsActivated,
        motorsDirection: MotorsDirection
    ) : this() {
        val hdr = HandActionHeader(StopMode.Torque)
        repr = listOf(hdr, torqueDetail, motorsActivated, motorsDirection).flatMap { it.toBytes() }
    }

    constructor(
        timeDetail: TimeStopModeDetail,
        motorsActivated: MotorsActivated,
        motorsDirection: MotorsDirection
    ) : this() {
        val hdr = HandActionHeader(StopMode.Time)
        repr = listOf(hdr, timeDetail, motorsActivated, motorsDirection).flatMap { it.toBytes() }
    }

    override fun toBytes(): Iterable<Byte> =
        LengthIndicator(repr.size.toByte()).toBytes() + repr

}