package info.hannes.github

import info.hannes.github.model.GithubVersion
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface IGithub {
    @GET("/repos/{githubUser}/{githubRepo}/releases")
    @Headers("Accept: application/json")
    fun getGithubVersions(
        @Path("githubUser") githubUser: String,
        @Path("githubRepo") githubRepo: String
    ): Call<List<GithubVersion>>
}
