package edu.nd.pmcburne.hello.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface PlacemarkApiService {
    @GET("placemarks.json")
    suspend fun getPlacemarks(): List<PlacemarkDto>
}

object NetworkModule {
    private const val BASE_URL = "https://www.cs.virginia.edu/~wxt4gm/"

    fun providePlacemarkApi(): PlacemarkApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(PlacemarkApiService::class.java)
    }
}
