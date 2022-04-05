package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.models.AddMedicationRequest
import com.app.okra.models.MedicationResponse
import com.app.okra.models.MedicationSearchResponse
import com.app.okra.models.MedicationUpdateRequest
import java.util.*

interface MedicationRepo {
    suspend fun getMedicationList(params: WeakHashMap<String, Any>): ApiResult<ApiData<MedicationResponse>>
    suspend fun searchMedication(search: String): ApiResult<ApiData<MedicationSearchResponse>>
    suspend fun addMedication(params: AddMedicationRequest): ApiResult<ApiData<Any>>
    suspend fun updateMedication(params: MedicationUpdateRequest): ApiResult<ApiData<Any>>
    suspend fun deleteMedication(id: String): ApiResult<ApiData<Any>>
}