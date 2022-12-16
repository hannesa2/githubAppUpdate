package info.hannes.appUpdate.github;

import java.util.List;

import info.hannes.appUpdate.model.GithubVersion;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface IGithub {

    @GET("/repos/{githubUser}/{githubRepo}/releases")
    @Headers("Accept: application/json")
    Call<List<GithubVersion>> getGithubVersions(
            @Path("githubUser") String githubUser,
            @Path("githubRepo") String githubRepo);

}
