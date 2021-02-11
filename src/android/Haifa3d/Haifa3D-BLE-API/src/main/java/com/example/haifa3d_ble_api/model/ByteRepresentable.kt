package com.example.haifa3d_ble_api.model

interface ByteRepresentable {
    fun toBytes(): Iterable<UByte>
}