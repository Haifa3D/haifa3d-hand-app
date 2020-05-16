package com.gjung.haifa3d

fun Iterable<Boolean>.toByte(): Byte {
    val lst = this.toList()
    if (lst.size > 8)
        throw IllegalArgumentException("Cant convert more than 8 Booleans to a byte.")
    var res = 0
    forEach{
        res = (res shl 1)
        res = res or (if (it) 1 else 0)
    }
    val offset = 8 - lst.size
    res = res shl offset
    return res.toByte()
}