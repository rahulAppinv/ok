package com.app.okra.ui.logbook.medication

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.amazonS3.AmazonS3
import com.app.okra.amazonS3.ImageBean
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.repo.MedicationRepo
import com.app.okra.models.*
import com.app.okra.utils.*
import com.app.sensor.amazonS3.AmazonS3Callbacks
import java.util.*

class MedicationViewModel(private val repo: MedicationRepo?) : BaseViewModel() , AmazonS3Callbacks {

    private var medicationLiveData = MutableLiveData<ApiData<MedicationResponse>>()
    val _medicationLiveData: LiveData<ApiData<MedicationResponse>>
        get() = medicationLiveData


    private var updateMedicationLiveData = MutableLiveData<ApiData<Any>>()
    val _updateMedicationLiveData: LiveData<ApiData<Any>>
        get() = updateMedicationLiveData

    private var addMedicationLiveData = MutableLiveData<ApiData<Any>>()
    val _addMedicationLiveData: LiveData<ApiData<Any>>
        get() = addMedicationLiveData

    private var deleteMedicationLiveData = MutableLiveData<ApiData<Any>>()
    val _deleteMedicationLiveData: LiveData<ApiData<Any>>
        get() = deleteMedicationLiveData

    private var searchMedicationLiveData = MutableLiveData<ApiData<MedicationSearchResponse>>()
    val _searchMedicationLiveData: LiveData<ApiData<MedicationSearchResponse>>
        get() = searchMedicationLiveData

    private var amazonStatusLiveData = MutableLiveData<ImageBean>()
    val _amazonStatusLiveData: LiveData<ImageBean>
        get() = amazonStatusLiveData


    var params = WeakHashMap<String, Any>()
    val updateRequest = MedicationUpdateRequest()
    val addRequest = AddMedicationRequest()
    private val mAmazonS3: AmazonS3 = AmazonS3()


    override fun uploadSuccess(imageBean: ImageBean) {
        amazonStatusLiveData.value = imageBean
        progressDialog.value = Event(ProgressDialogData(status = false))
    }

    override fun uploadError(e: Exception, imageBean: ImageBean) {
        progressDialog.value = Event(ProgressDialogData(status = false))
        errorObserver.value = Event(ApiData(message = e.message!!))
    }

    override fun uploadProgress(imageBean: ImageBean) {}

    override fun uploadFailed(imageBean: ImageBean) {
        progressDialog.value = Event(ProgressDialogData(status = false))
        errorObserver.value = Event(ApiData(message = MessageConstants.Errors.upload_failed))
    }

    fun prepareRequest(
        pageNo: Int,
        fromDate: String? = null,
        toDate: String? = null,
        type: String? = null
    ) {
        params.clear()
        params[AppConstants.RequestParam.pageNo] = pageNo
        params[AppConstants.RequestParam.limit] = AppConstants.DATA_LIMIT

        if (!fromDate.isNullOrEmpty()) {
            params[AppConstants.RequestParam.from] = getDifferentInfoFromDateInString(fromDate,
                    AppConstants.DateFormat.DATE_FORMAT_7, AppConstants.DateFormat.DATE_FORMAT_3)
        }
        if (!toDate.isNullOrEmpty()) {
            params[AppConstants.RequestParam.to] = getDifferentInfoFromDateInString(toDate,
                    AppConstants.DateFormat.DATE_FORMAT_7, AppConstants.DateFormat.DATE_FORMAT_3)
        }
      /*  if (!fromDate.isNullOrEmpty()) {
            params[AppConstants.RequestParam.from] = fromDate*//* getISOFromDate(fromDate,
                AppConstants.DateFormat.DATE_FORMAT_3
            )*//*
        }
        if (!toDate.isNullOrEmpty()) {
            params[AppConstants.RequestParam.to] = toDate *//*getISOFromDate(toDate,
                AppConstants.DateFormat.DATE_FORMAT_3
            )*//*
        }*/
        if (!type.isNullOrEmpty()) {
            params[AppConstants.RequestParam.type] = type
        }
    }

    fun prepareUpdateRequest(data  :MedicationData) {
        updateRequest.apply {
            medicationId = data._id
            medicineName = data.medicineName

            if (!data.unit.isNullOrEmpty ()) {
                unit = data.unit
            }

            if (data.quantity!=null) {
                quantity = data.quantity
            }

            if (data.tags!=null) {
                tags = data.tags
            }
            if (data.feelings!=null) {
                feelings = data.feelings
            }
            if (data.image!=null) {
                image = data.image
            }
        }
    }

    fun getMedicationList() {
        launchDataLoad {
            showProgressBar()
            val result = repo?.getMedicationList(params)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    medicationLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                is ApiResult.NetworkError -> {
                    errorObserver.value = Event(ApiData(message = "Network Issue"))
                }
            }
        }
    }

    fun addMedication(name: String, unit: String, quant: Int, medicationType: String) {
        launchDataLoad {
            showProgressBar()
            addRequest.medicineName = name
            addRequest.unit = unit
            addRequest.quantity = quant
            addRequest.medicineType = medicationType
            val result = repo?.addMedication(addRequest)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    addMedicationLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                else -> {
                    errorObserver.value = Event(ApiData(message = "Network Issue"))
                }
            }
        }
    }

    fun updateMedication() {
        launchDataLoad {
            showProgressBar()
            val result = repo?.updateMedication(updateRequest)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    updateMedicationLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                is ApiResult.NetworkError -> {
                    errorObserver.value = Event(ApiData(message = "Network Issue"))
                }
            }
        }
    }

    fun deleteMedication(id: String) {
        launchDataLoad {
            showProgressBar()
            val result = repo?.deleteMedication(id)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    deleteMedicationLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                is ApiResult.NetworkError -> {
                    errorObserver.value = Event(ApiData(message = "Network Issue"))
                }
            }
        }
    }



    fun searchMedication(search: String) {
        launchDataLoad {
            val result = repo?.searchMedication(search)
            when (result) {
                is ApiResult.Success -> {
                    searchMedicationLiveData.value = result.value
                }
                is ApiResult.GenericError -> {
                    errorObserver.value = Event(ApiData(message = result.message))
                }
                is ApiResult.NetworkError -> {
                    errorObserver.value = Event(ApiData(message = "Network Issue"))
                }
            }
        }
    }


    fun setAmazonCallback(activity : Activity){
        mAmazonS3.setCallback(activity, this)
    }


    fun uploadFile(uri: Uri?) {
        uploadFile(mAmazonS3, uri)
    }



}