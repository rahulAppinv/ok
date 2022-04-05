package com.app.okra.data.network

import com.app.okra.models.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

interface ApiService {

    @POST("v1/user/signup")
    suspend fun signUp(@Body request: InitialBoardingRequest): Response<BaseResponse<InitialBoardingResponse>>

    @POST("v1/user/login")
    suspend fun login(@Body request: InitialBoardingRequest): Response<BaseResponse<InitialBoardingResponse>>

    @POST("v1/user/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<BaseResponse<Any>>

    @POST("v1/user/verify-otp")
    suspend fun verifyOTP(@Body request: OTPVerifyRequest): Response<BaseResponse<InitialBoardingResponse>>

    @POST("v1/user/send-otp")
    suspend fun resendOTP(@Body request: ResendOtpRequest): Response<BaseResponse<Any>>

    @POST("v1/user/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<BaseResponse<Any>>

    @POST("v1/user/change-password")
    suspend fun changePassword(@Body request: ResetPasswordRequest): Response<BaseResponse<Any>>

    @POST("v1/user/logout")
    suspend fun logout(): Response<BaseResponse<Any>>


    @GET("v1/user/profile")
    suspend fun getUserProfile(@Query("userId")userId: String): Response<BaseResponse<UserDetailResponse>>

    @PUT("v1/user/profile-update")
    @FormUrlEncoded
    suspend fun updateProfile(@FieldMap params: WeakHashMap<String, Any>): Response<BaseResponse<Any>>

    @PUT("v1/user/settings")
    suspend fun modifySettings(@Body  body: SettingRequest):
            Response<BaseResponse<Any>>

    @GET("v1/user/contactus")
    suspend fun contactUs(): Response<BaseResponse<ContactResponse>>


    @GET("v1/user/support-request-message")
    suspend fun getSupportRequestList(@QueryMap params: WeakHashMap<String, Any>):
            Response<BaseResponse<List<SupportResponse>>>


    @POST("v1/user/support-request")
    @FormUrlEncoded
    suspend fun sendSupportRequest(@FieldMap params: WeakHashMap<String, Any>):
            Response<BaseResponse<Any>>

    @GET("v1/test")
    suspend fun getTestLogs(@QueryMap params: WeakHashMap<String, Any>): Response<BaseResponse<TestListResponse>>

    @GET("v1/meals")
    suspend fun getMealLogs(@QueryMap params: WeakHashMap<String, Any>): Response<BaseResponse<MealListResponse>>

    @GET("v1/test")
    suspend fun getTestDetails(@Query("testId") testId: String): Response<BaseResponse<TestListResponse>>

    @PUT("v1/test")
    suspend fun updateTest(@Body params: TestUpdateRequest): Response<BaseResponse<Any>>

    @DELETE("v1/test")
    suspend fun deleteTest(@Query("testId")  id:String): Response<BaseResponse<Any>>

    @POST("v1/test")
    suspend fun addTest(@Body  testData :TestAddRequest): Response<BaseResponse<Any>>

    @DELETE("v1/meals")
    suspend fun deleteMeal(@Query("mealsId")  id:String): Response<BaseResponse<Any>>

    @PUT("v1/meals")
    suspend fun updateMeal(@Body params: MealUpdateRequest): Response<BaseResponse<Any>>

    @GET("v1/test/download")
    suspend fun getDownloadUrl(@QueryMap queryMap: HashMap<String, String>): Response<BaseResponse<Any>>

    @GET
    suspend fun reportDownload(@Url url: String): ResponseBody

    @DELETE("v1/test/download")
    suspend fun deleteReport(@QueryMap queryMap: HashMap<String, String>): Response<BaseResponse<Any>>

    @Multipart
    @POST("v1/foodrecognition/full")
    suspend fun foodRecognition(@Part file: MultipartBody.Part? = null, @Query("user_key") key:String):
            Response<FoodRecognintionResponse>

    @POST("v1/user/reminder-setup")
    suspend fun setReminder(@Body  body: HashMap<String,Any>):
            Response<BaseResponse<Any>>

    @GET("v1/insight")
    suspend fun getInsight(@QueryMap params: WeakHashMap<String, Any>): Response<BaseResponse<InsightResponse>>

    @POST("v1/meals")
    suspend fun addMeal(@Body params: AddMealRequest): Response<BaseResponse<Any>>

    @GET("v1/user/dashboard")
    suspend fun dashboardInfo(@Query("timesOfConsideration")  time:String): Response<BaseResponse<HomeResponse>>

    @GET("v1/user/dashboard/strips-info")
    suspend fun stripeInfo(): Response<BaseResponse<HomeStripeResponse>>

    @GET("v1/user/device-list")
    suspend fun getPreviouslyConnectedDevices(): Response<BaseResponse<ArrayList<BLEDeviceListData>>>

    @POST("v1/user/device")
    suspend fun getTestDataCount(@Body deviceDataRequest: DeviceDataRequest): Response<BaseResponse<DeviceDataCount>>

    @GET("v1/user/notification")
    suspend fun notification(@Query("pageNo")  page:Int, @Query("limit") limit:Int): Response<BaseResponse<NotificationResponse>>

    @PUT("v1/user/notification-status-update")
    suspend fun deleteNotification(@Body  body: NotificationRequest): Response<BaseResponse<Any>>

    @GET("v1/users/medication")
    suspend fun getMedicationList(@QueryMap params: WeakHashMap<String, Any>): Response<BaseResponse<MedicationResponse>>

    @POST("v1/users/medication")
    suspend fun addMedication(@Body params: AddMedicationRequest): Response<BaseResponse<Any>>

    @PUT("v1/users/medication")
    suspend fun updateMedication(@Body params: MedicationUpdateRequest): Response<BaseResponse<Any>>

    @DELETE("v1/users/medication")
    suspend fun deleteMedication(@Query("medicationId")  id:String): Response<BaseResponse<Any>>

    @GET("v1/users/medication/search")
    suspend fun searchMedication(@Query("search") search:String): Response<BaseResponse<MedicationSearchResponse>>

    @GET("v1/test/download")
    suspend fun getReport(@QueryMap params: WeakHashMap<String, Any>): Response<BaseResponse<String>>

    @POST("v1/user/delete-user")
    @FormUrlEncoded
    suspend fun deleteAccountApi(@Field("email") email: String): Response<BaseResponse<String>>

}