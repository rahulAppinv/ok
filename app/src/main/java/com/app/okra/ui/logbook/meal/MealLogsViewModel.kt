package com.app.okra.ui.logbook.meal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.repo.MealLogsRepo
import com.app.okra.models.CommonData
import com.app.okra.models.FoodItemsRequest
import com.app.okra.models.MealListResponse
import com.app.okra.models.MealUpdateRequest
import com.app.okra.utils.*
import com.app.okra.utils.AppConstants.Companion.DATA_LIMIT
import java.util.*

class MealLogsViewModel(private val repo: MealLogsRepo?) : BaseViewModel() {


    private var mealLogLiveData = MutableLiveData<ApiData<MealListResponse>>()
    val _mealLogLiveData: LiveData<ApiData<MealListResponse>>
        get() = mealLogLiveData

   /* private var mealLogLiveData = MutableLiveData<ApiData<TestListResponse>>()
    val _mealLogLiveData: LiveData<ApiData<TestListResponse>>
        get() = mealLogLiveData
*/
    private var updateMealLiveData = MutableLiveData<ApiData<Any>>()
    val _updateMealLiveData: LiveData<ApiData<Any>>
        get() = updateMealLiveData

    private var deleteMealLiveData = MutableLiveData<ApiData<Any>>()
    val _deleteMealLiveData: LiveData<ApiData<Any>>
        get() = deleteMealLiveData

    var params= WeakHashMap<String, Any>()
    var updateRequest = MealUpdateRequest()

    fun prepareRequest(pageNo: Int,
                       fromDate: String?=null,
                       toDate: String?=null,
                       type: String?=null,
    ){
        params.clear()
        params[AppConstants.RequestParam.pageNo] = pageNo
        params[AppConstants.RequestParam.limit] = DATA_LIMIT

        if(!type.isNullOrEmpty())
             params[AppConstants.RequestParam.type] = type

        if(!fromDate.isNullOrEmpty()){
            params[AppConstants.RequestParam.fromDate] = getDifferentInfoFromDateInString(fromDate,
                    AppConstants.DateFormat.DATE_FORMAT_7, AppConstants.DateFormat.DATE_FORMAT_3)
        }
        if(!toDate.isNullOrEmpty()){
            params[AppConstants.RequestParam.toDate] = getDifferentInfoFromDateInString(toDate,
                    AppConstants.DateFormat.DATE_FORMAT_7, AppConstants.DateFormat.DATE_FORMAT_3)
        }
       /* if(!fromDate.isNullOrEmpty()){
            params[AppConstants.RequestParam.fromDate] = fromDate *//*getISOFromDate(fromDate,
                AppConstants.DateFormat.DATE_FORMAT_3, false
            )*//*
        }
        if(!toDate.isNullOrEmpty()){
            params[AppConstants.RequestParam.toDate] = toDate*//*getISOFromDate(toDate,
                AppConstants.DateFormat.DATE_FORMAT_3, false
            )*//*
        }*/

    }

    fun prepareUpdateRequest(
        mealsId: String?=null,
        date: String?=null,
        image: String?=null,
        foodItems: ArrayList<FoodItemsRequest>?=null,
        foodType: String?=null,
        calories: CommonData?=null,
        carbs: CommonData?=null,
        fat: CommonData?=null,
        protein: CommonData?=null,
        noOfServing: String?=null,
    ){
        updateRequest = MealUpdateRequest()

        updateRequest.mealsId= mealsId

        if(!date.isNullOrEmpty()){
            updateRequest.date = date
        }

        image?.let{
            updateRequest.image = it
        }
        foodItems?.let{
            updateRequest.foodItems = it
        }
        calories?.let{
            updateRequest.calories = it
        }
        carbs?.let{
            updateRequest.carbs = it
        }
        fat?.let{
            updateRequest.fat = it
        }
        protein?.let{
            updateRequest.protien = it
        }
        foodType?.let{
            updateRequest.foodType = it
        }

        if(!noOfServing.isNullOrEmpty()){
            updateRequest.noOfServings=noOfServing
        }
    }

    fun getMealLogs() {
        launchDataLoad {
            showProgressBar()
            val result = repo?.getMealLogs(params)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    mealLogLiveData.value = result.value
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

    fun updateMeal() {
        launchDataLoad {
            if(validateRequest(updateRequest)) {
                showProgressBar()
                val result = repo?.updateMealLog(updateRequest)
                hideProgressBar()
                when (result) {
                    is ApiResult.Success -> {
                        updateMealLiveData.value = result.value
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

    private fun validateRequest(updateRequest: MealUpdateRequest): Boolean {
      return when{
          updateRequest.image.isNullOrBlank() ->{
              toastObserver.value = Event(ToastData(MessageConstants.Errors.please_select_image))
              false
          }
          updateRequest.foodType.isNullOrBlank() ->{
              toastObserver.value = Event(ToastData(MessageConstants.Errors.please_select_food_type))
              false
          }
          updateRequest.date.isNullOrBlank() ->{
              toastObserver.value = Event(ToastData(MessageConstants.Errors.please_select_date))
              false
          }
          updateRequest.calories == null ->{
              toastObserver.value = Event(ToastData(MessageConstants.Errors.please_select_calories))
              false
          }
          updateRequest.carbs == null ->{
              toastObserver.value = Event(ToastData(MessageConstants.Errors.please_select_carbs))
              false
          }
          updateRequest.fat == null ->{
              toastObserver.value = Event(ToastData(MessageConstants.Errors.please_select_fat))
              false
          }
          updateRequest.protien == null ->{
              toastObserver.value = Event(ToastData(MessageConstants.Errors.please_select_protein))
              false
          }
          else -> true
      }
    }

    fun deleteMeal(id :String) {
        launchDataLoad {
            showProgressBar()
            val result = repo?.deleteMeal(id)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    deleteMealLiveData.value = result.value
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

}