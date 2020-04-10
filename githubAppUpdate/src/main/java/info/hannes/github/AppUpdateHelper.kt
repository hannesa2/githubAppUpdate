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
    fun checkForNewVersion(activity: AppCompatActivity, gitUser: String, gitRepo: String, versionName: String) = activity.lifecycle.coroutineScope.launch(Dispatchers.Main) {
        val key = "LAST_VERSION_CHECK"
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        if (prefs.getLong(key, 0) < System.currentTimeMillis() - 1000 * 3600 * 24 / 24 / 60 * 5) {
            try {
                val logEntries = withContext(Dispatchers.Default) {
                    val client = GithubClient(HttpLoggingInterceptor.Level.BODY)
                    client.github.getGithubVersions(gitUser, gitRepo).execute()
                }
                prefs.edit().putLong(key, System.currentTimeMillis()).apply()

                val latestRelease = logEntries.body()?.firstOrNull()
                latestRelease?.let {
                    Log.d("AppUpdateHelper", it.tagName + " > " + versionName + " " + (it.tagName > versionName))
                    if (it.tagName > versionName) {
                        AlertDialog.Builder(activity)
                                .setTitle("New Version")
                                .setMessage("There is a new version ${it.tagName} on Github. Do you want to download it ?")
                                .setOnCancelListener { dialog ->
                                    dialog.dismiss()
                                }
                                .setNegativeButton(activity.getString(android.R.string.no)) { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .setPositiveButton(activity.getString(android.R.string.yes)) { dialog, _ ->
                                    val uriUrl = Uri.parse(it.htmlUrl)
                                    activity.startActivity(Intent(Intent.ACTION_VIEW, uriUrl))
                                    dialog.dismiss()
                                }
                                .show()
                    }
                }
            } catch (e: Exception) {
                Log.e("AppUpdateHelper", "git check deliver: ${e.message}")
                Toast.makeText(activity, "git check deliver: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}