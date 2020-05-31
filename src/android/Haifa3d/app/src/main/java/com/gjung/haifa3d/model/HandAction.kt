package com.gjung.haifa3d.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

class HandAction(val Movements: Collection<HandMovement>) : ByteRepresentable {
    constructor(
        vararg movements: HandMovement
    ) : this(movements.toList())

    override fun toBytes(): Iterable<Byte> {
        val repr = Movements.flatMap { it.toBytes() }
        return LengthIndicator((repr.size + 1).toByte()).toBytes() + repr
    }
}