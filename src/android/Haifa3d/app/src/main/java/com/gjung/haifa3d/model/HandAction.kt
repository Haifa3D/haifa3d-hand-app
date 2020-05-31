package com.gjung.haifa3d.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class HandAction() : Parcelable, ByteRepresentable {
    private lateinit var repr: List<Byte>

    constructor(
        movements: Iterable<HandMovement>
    ) : this() {
        repr = movements.flatMap { it.toBytes() }
    }

    constructor(
        vararg movements: HandMovement
    ) : this() {
        repr = movements.flatMap { it.toBytes() }
    }

    override fun toBytes(): Iterable<Byte> =
        LengthIndicator((repr.size + 1).toByte()).toBytes() + repr

}