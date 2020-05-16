package com.gjung.haifa3d.model

class LengthIndicator(val length: Byte): ByteRepresentable {
    override fun toBytes(): Iterable<Byte> = listOf(length)
}