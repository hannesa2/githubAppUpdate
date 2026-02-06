package info.hannes.github

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object GithubRestServiceCreationHelper {

    private var httpLoggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()

    init {
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
    }

    private fun createGson(): Gson {
        return GsonBuilder().serializeNulls()
            .create()
    }

    fun <T> createGithubService(retrofitInterface: Class<T>, logLevel: HttpLoggingInterceptor.Level, token: String? = null): T {

        httpLoggingInterceptor.level = logLevel

        val clientHttp = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)

        token?.let {
            clientHttp.addInterceptor(TokenInterceptor(it))
        }

        val client = clientHttp
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
