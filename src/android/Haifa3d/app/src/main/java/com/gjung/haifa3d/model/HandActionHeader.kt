package com.gjung.haifa3d.model

import com.gjung.haifa3d.toByte

class HandActionHeader(val stopMode: StopMode): ByteRepresentable {
    override fun toBytes(): Iterable<Byte> =
        listOf(listOf(stopMode.isBitSet).toByte())

}