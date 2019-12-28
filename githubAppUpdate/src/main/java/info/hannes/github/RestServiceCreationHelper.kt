package info.hannes.github

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RestServiceCreationHelper {

    private var httpLoggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()

    init {
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
    }

    private fun createGson(): Gson {
        return GsonBuilder().serializeNulls()
            .create()
    }

    fun <T> createGithubService(retrofitInterface: Class<T>, logLevel: HttpLoggingInterceptor.Level): T {

        httpLoggingInterceptor.level = logLevel

        val client = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val gson = createGson()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(retrofitInterface)
    }

}
