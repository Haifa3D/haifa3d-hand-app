package com.gjung.haifa3d

import android.bluetooth.BluetoothDevice
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.gjung.haifa3d.model.HandAction
import no.nordicsemi.android.ble.ReadRequest
import no.nordicsemi.android.ble.Request
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.callback.DataSentCallback
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

suspend fun WriteRequest.sendSuspend(): Unit =
    suspendCoroutine { cont ->
        val callback = DataSentCallback { _, _ -> cont.resume(Unit) }

        this.with(callback)
            .enqueue()
    }

fun <T> Fragment.getNavigationResult(key: String = "result") =
    findNavController().currentBackStackEntry?.savedStateHandle?.get<T>(key)

fun <T> Fragment.getNavigationResultLiveData(key: String = "result") =
    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key)

fun <T> Fragment.setNavigationResult(result: T, key: String = "result") {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(key, result)
}

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}