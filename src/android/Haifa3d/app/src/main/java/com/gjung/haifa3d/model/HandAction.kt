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

fun ByteArray.decodeHandAction(): HandAction {
    if ((this.size - 1).rem(4) != 0)
        java.lang.IllegalArgumentException("Size of the array needs to be 4n+1.")
    val movementCount = (this.size - 1) / 4
    val movements = mutableListOf<HandMovement>()
    for (x in 0 until movementCount) {
        val base = 1 + 4 * x
        movements.add(
            HandMovement(
                this[base + 0].decodeTorqueStopModeDetail(),
                TimeStopModeDetail(this[base + 1]),
                this[base + 2].decodeMotorsActivated(),
                this[base + 2].decodeMotorsDirection()
            )
        )
    }
    return HandAction(movements)
}