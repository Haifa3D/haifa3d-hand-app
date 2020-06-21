package com.gjung.haifa3d.model

interface ByteRepresentable {
    fun toBytes(): Iterable<UByte>
}