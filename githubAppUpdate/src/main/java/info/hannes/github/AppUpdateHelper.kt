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
    fun checkForNewVersion(activity: AppCompatActivity, gitUser: String, gitRepo: String, versionName: String, callback: ((String) -> Unit)? = null, force: Boolean = false
    ) =
            activity.lifecycle.coroutineScope.launch(Dispatchers.Main) {
                val key = "LAST_VERSION_CHECK"
                val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
                if (force || prefs.getLong(key, 0) < System.currentTimeMillis() - 1000 * 3600 * 24 / 24 / 60 * 5) {
                    try {
                        val logEntries = withContext(Dispatchers.Default) {
                            val client = GithubClient(HttpLoggingInterceptor.Level.BODY)
                            client.github.getGithubVersions(gitUser, gitRepo).execute()
                        }
                        prefs.edit().putLong(key, System.currentTimeMillis()).apply()

                        val latestRelease = logEntries.body()?.firstOrNull()
                        latestRelease?.let {
                            val assetApk = it.assets.find { it.name.endsWith("release.apk") }

                            Log.d("AppUpdateHelper", it.tagName + " > " + versionName + " " + (it.tagName > versionName))
                            callback?.invoke(it.tagName)
                            if (it.tagName > versionName) {
                                val dialog = AlertDialog.Builder(activity)
                                        .setTitle("New Version on Github")
                                        .setMessage("You use version \n$versionName\n" +
                                                "and there is a new version \n${it.tagName}\n" +
                                                "Do you want to download it ?")
                                        .setOnCancelListener { dialog ->
                                            dialog.dismiss()
                                        }
                                        .setNegativeButton(activity.getString(android.R.string.no)) { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                        .setPositiveButton(activity.getString(R.string.showRelease)) { dialog, _ ->
                                            val uriUrl = Uri.parse(it.htmlUrl)
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
                                callback?.invoke("Nothing to do with ${it.tagName}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AppUpdateHelper", "git check deliver: ${e.message}")
                        Toast.makeText(activity, "git check deliver: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
}