package com.app.okra.data.repo

import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.network.ApiService
import com.app.okra.data.network.BaseRepo
import com.app.okra.models.*
import kotlinx.coroutines.Dispatchers
import java.util.*

class MedicationRepoImpl constructor(
        private val apiService: ApiService,
) : BaseRepo(apiService),
    MedicationRepo {

    override suspend fun getMedicationList(params: WeakHashMap<String, Any>): ApiResult<ApiData<MedicationResponse>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.getMedicationList(params)
        }
    }

    override suspend fun searchMedication(search: String): ApiResult<ApiData<MedicationSearchResponse>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.searchMedication(search)
        }
    }

    override suspend fun addMedication(params: AddMedicationRequest): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.addMedication(params)
        }
    }

        override suspend fun updateMedication(params: MedicationUpdateRequest): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.updateMedication(params)
        }
    }

    override suspend fun deleteMedication(id: String): ApiResult<ApiData<Any>> {
        return safeApiCall(Dispatchers.IO) {
            apiService.deleteMedication(id)
        }
    }
}