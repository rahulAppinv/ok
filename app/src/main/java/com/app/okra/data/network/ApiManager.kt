package com.app.okra.data.network


import com.app.okra.BuildConfig
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.utils.AppConstants
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.*
import java.util.concurrent.TimeUnit

object ApiManager {
    private val retrofit: Retrofit
    private val retrofitAuth: Retrofit
    private val retrofitCalorieMama: Retrofit

    init {
        retrofit = httpClient
        retrofitAuth = httpClientAuth
        retrofitCalorieMama = httpClientCalorieMama
    }

    private val httpClient: Retrofit
        get() {
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(getHttpClient().build())
                .build()
        }

    private val httpClientAuth: Retrofit
        get() {
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(getHttpClientAuth().build())
                .build()
        }

    private val httpClientCalorieMama: Retrofit
        get() {
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api-2445582032290.production.gw.apicast.io/")
                .client(getHttpClientCalorieMama().build())
                .build()
        }

    private fun getHttpClientAuth(): OkHttpClient.Builder {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        return OkHttpClient.Builder().addInterceptor { chain ->
            val original = chain.request()

            val tz: TimeZone = TimeZone.getDefault()
            val requestBuilder: Request.Builder = original.newBuilder()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer "+PreferenceManager.getString(AppConstants.Pref_Key.ACCESS_TOKEN))
                .header("platform", "1")
                .header("api_key","1234")
                .header("language", "en")
                .header("timezone", tz.id)
                .header("offset", tz.rawOffset.toString())
            val request = requestBuilder.build()
            val response = chain.proceed(request)
            response
        }
            .addInterceptor(httpLoggingInterceptor.apply {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            })
            .readTimeout(30000, TimeUnit.MILLISECONDS)
            .writeTimeout(30000, TimeUnit.MILLISECONDS)

    }


    /**
     * Method to create [OkHttpClient] builder by adding required headers in the [Request]
     * @return OkHttpClient object
     */
    private fun getHttpClient(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder: Request.Builder = original.newBuilder()
                    .header("Accept", "application/json")
                    .header("Authorization", Credentials.basic("okra","okra@123"))
                    .header("platform", "1")
                    .header("language", "en")
                    .header("offset", getOffset())
                    .header("timezone", getTimeZone())
                    .method(original.method, original.body)
                val request = requestBuilder.build()
                val response = chain.proceed(request)
                response
            }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            /*.addInterceptor(Interceptor { chain ->
                val request: Request = chain.request()
                val response: Response = chain.proceed(request)

                if (!response.isSuccessful) {
                    var errorMessage = "Server error, contact support"

                    try{
                        errorMessage = JSONObject(response.body?.string() ?: "").toString()
                        println(":::::: ${errorMessage}")
                    }catch (ex: Exception){
                        ex.printStackTrace()
                    }

                    return@Interceptor response
                }
                response
            }
            )*/
            .readTimeout(30000, TimeUnit.MILLISECONDS)
            .writeTimeout(30000, TimeUnit.MILLISECONDS)
    }

    private fun getLoggingInterceptor(): HttpLoggingInterceptor {
        return if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor(CustomHttpLogger())
            httpLoggingInterceptor.apply {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            }
        } else {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.apply {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    private fun getOffset(): String {
        val tz = TimeZone.getDefault()
        val cal = GregorianCalendar.getInstance(tz)
        return tz.getOffset(cal.timeInMillis).toString()
    }

    private fun getTimeZone(): String {
       return TimeZone.getDefault().id
    }

    fun getRetrofit() : ApiService = retrofit.create(ApiService::class.java)
    fun getRetrofitAuth() : ApiService = retrofitAuth.create(ApiService::class.java)
    fun getRetrofitCalorieMama() : ApiService = retrofitCalorieMama.create(ApiService::class.java)

    private fun getHttpClientCalorieMama(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder: Request.Builder = original.newBuilder()
                    .method(original.method, original.body)
                val request = requestBuilder.build()
                val response = chain.proceed(request)
                response
            }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .readTimeout(30000, TimeUnit.MILLISECONDS)
            .writeTimeout(30000, TimeUnit.MILLISECONDS)
    }

}