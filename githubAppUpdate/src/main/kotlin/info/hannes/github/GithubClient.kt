package info.hannes.github

import okhttp3.logging.HttpLoggingInterceptor

internal class GithubClient(private val logLevel: HttpLoggingInterceptor.Level, private val token: String? = null) {

    val github: IGithub

    private val githubService: IGithub
        get() = GithubRestServiceCreationHelper.createGithubService(
            IGithub::class.java,
            logLevel,
            token
        )

    init {
        github = githubService
    }

}
