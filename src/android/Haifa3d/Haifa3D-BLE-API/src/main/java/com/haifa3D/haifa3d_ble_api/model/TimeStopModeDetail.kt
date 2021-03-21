package com.haifa3D.haifa3d_ble_api.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

const val MS_PER_TIME_UNIT = 50

@Keep
@Parcelize
data class TimeStopModeDetail(val durationTimeUnitCount: UByte): ByteRepresentable, Parcelable {
    val durationMs
        get() = durationTimeUnitCount.toInt() * MS_PER_TIME_UNIT
    override fun toBytes(): Iterable<UByte> =
        listOf(durationTimeUnitCount)
}