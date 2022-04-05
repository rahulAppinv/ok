package com.app.okra.ui.reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.repo.ReportRepo
import com.app.okra.utils.*
import com.app.okra.utils.AppConstants.DateFormat.DATE_FORMAT_10
import java.util.*

class ReportsViewModel(private val repo: ReportRepo?): BaseViewModel() {


    val fileTypeArray = arrayOf("XLSX", "PDF")
    lateinit var selectedFileType: String
    var startDateTimestamp = MutableLiveData(0L)
    var endDateTimestamp = MutableLiveData(0L)
    val isStartDateSetMLD = MutableLiveData(false)
    val isEndDateSetMLD = MutableLiveData(false)
    val isFileTypeSetMLD = MutableLiveData(false)
    val areAllFieldsSet = MutableLiveData(false)
    val hMap =  WeakHashMap<String, Any>()

    private var reportLiveData = MutableLiveData<ApiData<String>>()
    val _reportLiveData: LiveData<ApiData<String>>
        get() = reportLiveData



    fun setReportRequest(sDate: String?, eDate: String?, type: String?) {
        hMap.clear()
        sDate?.let{
            hMap[AppConstants.RequestParam.from]= getDifferentInfoFromDateInString(it, AppConstants.DateFormat.DATE_FORMAT_8, DATE_FORMAT_10)
        }
        eDate?.let{
            hMap[AppConstants.RequestParam.to]= getDifferentInfoFromDateInString(it, AppConstants.DateFormat.DATE_FORMAT_8, DATE_FORMAT_10)
        }
        type?.let{
            hMap[AppConstants.RequestParam.type]= getTypeOfDoc(it)
        }

    }

    private fun getTypeOfDoc(type: String): String {
        return if(type == fileTypeArray[0]){
            AppConstants.TYPE_EXCEL
        }else{
            AppConstants.TYPE_PDF
        }
    }

    fun getReportUrl() {
        launchDataLoad {
            showProgressBar()
            val result = repo?.getReportUrl(hMap)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    reportLiveData.value = result.value
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
}