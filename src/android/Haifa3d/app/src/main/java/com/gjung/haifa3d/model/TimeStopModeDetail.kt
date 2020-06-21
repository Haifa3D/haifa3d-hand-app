package com.gjung.haifa3d.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class TimeStopModeDetail(val durationTimeUnitCount: UByte): ByteRepresentable, Parcelable {
    override fun toBytes(): Iterable<UByte> =
        listOf(durationTimeUnitCount)
}