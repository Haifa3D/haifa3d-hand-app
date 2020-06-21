package com.gjung.haifa3d.model

class LengthIndicator(val length: UByte): ByteRepresentable {
    override fun toBytes(): Iterable<UByte> = listOf(length)
}