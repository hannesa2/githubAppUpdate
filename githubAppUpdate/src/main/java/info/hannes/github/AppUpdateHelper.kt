package info.hannes.github

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.preference.PreferenceManager
import info.hannes.github.model.Asset
import info.hannes.github.model.GithubVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response


object AppUpdateHelper {

    private fun Activity.getVersionName(): String = try {
        this.getPackageInfo().versionName ?: ""
    } catch (e: PackageManager.NameNotFoundException) {
        ""
    }

    @Suppress("DEPRECATION")
    fun Context.getPackageInfo(): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
    }

    fun checkForNewVersion(
        activity: AppCompatActivity,
        gitRepoUrl: String,
        callback: ((String) -> Unit)? = null,
        force: Boolean = false
    ) = activity.lifecycle.coroutineScope.launch(Dispatchers.Main) {

        val currentVersionName = activity.getVersionName()

        val key = "LAST_VERSION_CHECK"
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

        if (force || prefs.getLong(key, 0) < System.currentTimeMillis() - 1000 * 3600 * 24 / 24 / 60 * 5) {
            try {
                val versionList = requestVersions(gitRepoUrl)
                prefs.edit().putLong(key, System.currentTimeMillis()).apply()

                versionList.body()?.firstOrNull()?.let { release ->
                    val assetApk = release.assets.find { it.name.endsWith("release.apk") }

                    Log.d("AppUpdateHelper", release.tagName + " > " + currentVersionName + " " + (release.tagName > currentVersionName))
                    callback?.invoke(release.tagName)
                    if (release.tagName > currentVersionName) {
                        askUser(activity, currentVersionName, release, assetApk)
                    } else {
                        callback?.invoke("Nothing to do with ${release.tagName}")
                    }
                }
            } catch (e: Exception) {
                Log.e("AppUpdateHelper", "git check deliver: ${e.message}")
                Toast.makeText(activity, "git check delivers: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun requestVersions(gitRepoUrl: String): Response<MutableList<GithubVersion>> {
        val versionList = withContext(Dispatchers.Default) {
            val client = GithubClient(HttpLoggingInterceptor.Level.BODY)
            client.github.getGithubVersions(gitRepoUrl.user(), gitRepoUrl.repo()).execute()
        }
        return versionList
    }

    private fun askUser(
        activity: AppCompatActivity,
        currentVersionName: String,
        release: GithubVersion,
        assetApk: Asset?
    ): AlertDialog? {
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
        return dialog.show()
    }
}