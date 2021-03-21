package com.haifa3D.haifa3d_ble_api.model

interface ByteRepresentable {
    fun toBytes(): Iterable<UByte>
}