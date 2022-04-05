package com.app.okra.ui.add_meal

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.okra.amazonS3.AmazonS3
import com.app.okra.amazonS3.ImageBean
import com.app.okra.base.BaseViewModel
import com.app.okra.data.network.ApiData
import com.app.okra.data.network.ApiResult
import com.app.okra.data.repo.AddMealRepo
import com.app.okra.models.AddMealRequest
import com.app.okra.models.CommonData
import com.app.okra.models.FoodItemsRequest
import com.app.okra.models.FoodRecognintionResponse
import com.app.okra.utils.Event
import com.app.okra.utils.MessageConstants
import com.app.okra.utils.ProgressDialogData
import com.app.sensor.amazonS3.AmazonS3Callbacks
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URLConnection

class AddMealViewModel(private val repo: AddMealRepo?) : BaseViewModel(), AmazonS3Callbacks {

    private var foodRecognitionLiveData = MutableLiveData<FoodRecognintionResponse>()
    val _foodRecognitionLiveData: LiveData<FoodRecognintionResponse>
        get() = foodRecognitionLiveData
    private lateinit var multipart: MultipartBody.Part

    private var addMealLiveData = MutableLiveData<ApiData<Any>>()
    val _addMealLiveData: LiveData<ApiData<Any>>
        get() = addMealLiveData

    private var amazonStatusLiveData = MutableLiveData<ImageBean>()
    val _amazonStatusLiveData: LiveData<ImageBean>
        get() = amazonStatusLiveData


    val addRequest = AddMealRequest()
    private val mAmazonS3: AmazonS3 = AmazonS3()


    fun setAmazonCallback(activity : Activity){
        mAmazonS3.setCallback(activity, this)
    }


    fun uploadFile(uri: Uri?) {
        uploadFile(mAmazonS3, uri)
    }



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

    fun foodRecognition(imageUri: String?) {
        launchDataLoad {
            showProgressBar()
            multipart = getImagePart(File(imageUri), "file")
            val result = repo?.foodRecognition(multipart,"47c0f9ca2889db9294ee65c2fd4ccfaa")
            hideProgressBar()
            when (result) {
                is ApiResult.Success1<*> -> {
                    foodRecognitionLiveData.value = result.value as FoodRecognintionResponse
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

    fun getImagePart(file: File, str: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            str,
            file.name,
            file.asRequestBody(URLConnection.guessContentTypeFromName(file.name).toMediaTypeOrNull())
        )
    }

    fun prepareAddRequest(
        date: String?=null,
        image: String?=null,
        foodItems: ArrayList<FoodItemsRequest>?=null,
        foodType: String?=null,
        calories: CommonData?=null,
        carbs: CommonData?=null,
        fat: CommonData?=null,
        protein: CommonData?=null,
        noOfServings: String?=null,
    ){
        if(!date.isNullOrEmpty()){
            addRequest.date = date
        }

        image?.let{
            addRequest.image = it
        }
        foodItems?.let{
            addRequest.foodItems = it
        }
        calories?.let{
            addRequest.calories = it
        }
        carbs?.let{
            addRequest.carbs = it
        }
        fat?.let{
            addRequest.fat = it
        }
        protein?.let{
            addRequest.protien = it
        }
        foodType?.let{
            addRequest.foodType = it
        }

        if(!noOfServings.isNullOrEmpty()){
            addRequest.noOfServings = noOfServings
        }
    }

    fun addMeal() {
        launchDataLoad {
            showProgressBar()
            val result = repo?.addMealLog(addRequest)
            hideProgressBar()
            when (result) {
                is ApiResult.Success -> {
                    addMealLiveData.value = result.value
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