package info.hannes.github

import okhttp3.logging.HttpLoggingInterceptor

internal class GithubClient(private val logLevel: HttpLoggingInterceptor.Level) {

    val github: IGithub

    private val githubService: IGithub
        get() = RestServiceCreationHelper.createGithubService(
                IGithub::class.java,
                logLevel
        )

    init {
        github = githubService
    }

}
