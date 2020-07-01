package com.gjung.haifa3d.data

import androidx.room.TypeConverter
import com.gjung.haifa3d.model.HandAction
import com.gjung.haifa3d.model.decodeHandAction

@ExperimentalUnsignedTypes
class Converters {
    @TypeConverter fun handActionToUByteArray(handAction: HandAction): ByteArray =
        handAction.toBytes().toList().toUByteArray().toByteArray()

    @TypeConverter fun uByteArrayToHandAction(bytes: ByteArray): HandAction =
        bytes.toUByteArray().decodeHandAction()
}