package com.haifa3D.haifa3d_ble_api.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class HandMovement(
    val torqueDetail: TorqueStopModeDetail,
    val timeDetail: TimeStopModeDetail,
    val motorsActivated: MotorsActivated,
    val motorsDirection: MotorsDirection
) : ByteRepresentable, Parcelable {
    override fun toBytes(): Iterable<UByte> = listOf<ByteRepresentable>(torqueDetail, timeDetail, motorsActivated, motorsDirection).flatMap { it.toBytes() }
}