package com.haifa3D.haifa3d_ble_api.model

class HandAction(val Movements: MutableList<HandMovement>) : ByteRepresentable {
    constructor(
        vararg movements: HandMovement
    ) : this(movements.toMutableList())

    override fun toBytes(): Iterable<UByte> {
        val repr = Movements.flatMap { it.toBytes() }
        return LengthIndicator((repr.size + 1).toUByte()).toBytes() + repr
    }

    companion object {
        val Empty
            get() = HandAction()
    }
}

fun UByteArray.decodeHandAction(): HandAction {
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
                this[base + 3].decodeMotorsDirection()
            )
        )
    }
    return HandAction(movements)
}