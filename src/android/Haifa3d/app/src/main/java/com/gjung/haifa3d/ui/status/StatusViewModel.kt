package com.gjung.haifa3d.ui.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StatusViewModel : ViewModel() {
    val batteryPercentage: MutableLiveData<Int> = MutableLiveData<Int>().apply {
        value = 0
    }
}