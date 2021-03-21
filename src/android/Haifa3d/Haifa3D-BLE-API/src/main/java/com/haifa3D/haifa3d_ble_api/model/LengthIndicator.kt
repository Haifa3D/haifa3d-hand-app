package com.haifa3D.haifa3d_ble_api.model

class LengthIndicator(val length: UByte): ByteRepresentable {
    override fun toBytes(): Iterable<UByte> = listOf(length)
}