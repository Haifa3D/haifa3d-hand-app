package com.gjung.haifa3d

import android.bluetooth.BluetoothDevice
import com.gjung.haifa3d.model.HandAction
import no.nordicsemi.android.ble.ReadRequest
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.data.Data
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Iterable<Boolean>.toByte(): Byte {
    val lst = this.toList()
    if (lst.size > 8)
        throw IllegalArgumentException("Cant convert more than 8 Booleans to a byte.")
    var res = 0
    lst.forEach{
        res = (res shl 1)
        res = res or (if (it) 1 else 0)
    }
    val offset = 8 - lst.size
    res = res shl offset
    return res.toByte()
}

fun Byte.toBits(): List<Boolean> {
    val int = this.toInt()
    return listOf(
        int and 128 == 128, // msb
        int and 64 == 64,
        int and 32 == 32,
        int and 16 == 16,
        int and 8 == 8,
        int and 4 == 4,
        int and 2 == 2,
        int and 1 == 1  // lsb
    )
}

suspend fun ReadRequest.readBytesAsync(): ByteArray? =
    suspendCoroutine { cont ->
        val callback = DataReceivedCallback { _, data -> cont.resume(data.value) }

        this.with(callback)
            .enqueue()
    }
