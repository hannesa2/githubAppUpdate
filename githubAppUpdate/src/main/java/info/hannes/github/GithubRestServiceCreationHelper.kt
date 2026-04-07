package info.hannes.github

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

internal object GithubRestServiceCreationHelper {

    private var httpLoggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    init {
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
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

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF-8".toMediaType()))
            .build()

        return retrofit.create(retrofitInterface)
    }

}
