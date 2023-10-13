package Services.Network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BaseService(baseUrl: String?) { // bunu null able yaptın sonrasında bak değiştirmek gerekebilir
    private val retrofit: Retrofit

    init {
        val httpClient = OkHttpClient.Builder()
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}