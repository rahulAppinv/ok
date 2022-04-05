package com.app.okra.utils

import androidx.lifecycle.MutableLiveData
import com.app.okra.models.MedicationData

object EventLiveData {

     var eventLiveData = MutableLiveData<Event<EventData>>()
     var editMedicationLiveData = MutableLiveData<Event<MedicationData>>()

    data class EventData(val type:String?=null,val data:Any?=null)
}

