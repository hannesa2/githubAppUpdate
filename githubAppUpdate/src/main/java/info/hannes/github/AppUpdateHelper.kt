package info.hannes.github

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.logging.HttpLoggingInterceptor

object AppUpdateHelper {
    fun checkForNewVersion(activity: AppCompatActivity, gitRepoUrl: String, currentVersionName: String, callback: ((String) -> Unit)? = null, force: Boolean = false
    ) = activity.lifecycle.coroutineScope.launch(Dispatchers.Main) {
        val key = "LAST_VERSION_CHECK"
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val gitRepoUrlHttps = gitRepoUrl.replace("git@", "https//")
            .replace(":", "/")
            .replace("///", "//")
            .replace("https//", "https://").split("/")
        val gitUser = gitRepoUrlHttps[3]
        val gitRepo = gitRepoUrlHttps[4]
        if (force || prefs.getLong(key, 0) < System.currentTimeMillis() - 1000 * 3600 * 24 / 24 / 60 * 5) {
            try {
                val versionList = withContext(Dispatchers.Default) {
                    val client = GithubClient(HttpLoggingInterceptor.Level.BODY)
                    client.github.getGithubVersions(gitUser, gitRepo).execute()
                }
                prefs.edit().putLong(key, System.currentTimeMillis()).apply()

                val latestRelease = versionList.body()?.firstOrNull()
                latestRelease?.let { release ->
                    val assetApk = release.assets.find { it.name.endsWith("release.apk") }

                    Log.d("AppUpdateHelper", release.tagName + " > " + currentVersionName + " " + (release.tagName > currentVersionName))
                    callback?.invoke(release.tagName)
                    if (release.tagName > currentVersionName) {
                        @Suppress("DEPRECATION")
                        val dialog = AlertDialog.Builder(activity)
                            .setTitle("New Version on Github")
                            .setMessage(
                                "You use version \n$currentVersionName\n" +
                                        "and there is a new version \n${release.tagName}\n" +
                                        "Do you want to download it ?"
                            )
                            .setOnCancelListener { dialog ->
                                dialog.dismiss()
                            }
                            .setNegativeButton(activity.getString(android.R.string.no)) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .setPositiveButton(activity.getString(R.string.showRelease)) { dialog, _ ->
                                val uriUrl = Uri.parse(release.htmlUrl)
                                Log.d("open", uriUrl.toString())
                                activity.startActivity(Intent(Intent.ACTION_VIEW, uriUrl))
                                dialog.dismiss()
                            }

                        assetApk?.let {
                            dialog.setNeutralButton(activity.getString(R.string.directDownload)) { dialog, _ ->
                                val uriUrl = Uri.parse(it.browserDownloadUrl)
                                Log.d("open", uriUrl.toString())
                                activity.startActivity(Intent(Intent.ACTION_VIEW, uriUrl))
                                dialog.dismiss()
                            }
                        }
                        dialog.show()
                    } else {
                        callback?.invoke("Nothing to do with ${release.tagName}")
                    }
                }
            } catch (e: Exception) {
                Log.e("AppUpdateHelper", "git check deliver: ${e.message}")
                Toast.makeText(activity, "git check deliver: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}