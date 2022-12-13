package info.hannes.github

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import timber.log.Timber
import java.lang.RuntimeException
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit


object AppUpdateHelper {

    private fun Activity.getVersionName(): String = try {
        this.getPackageInfo().versionName ?: ""
    } catch (e: PackageManager.NameNotFoundException) {
        ""
    }

    private fun Context.getPackageInfo(): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
    }

    // silently in background
    fun checkForNewVersion(activity: AppCompatActivity, gitRepoUrl: String, repeatTime : Long = 6, timeUnit: TimeUnit = TimeUnit.HOURS, token: String? = null) {
        val currentVersionName = activity.getVersionName()
        DownloadWorker.run(activity, currentVersionName, gitRepoUrl, repeatTime, timeUnit, token)
    }

    fun checkWithDialog(
        activity: AppCompatActivity,
        gitRepoUrl: String,
        callback: ((String) -> Unit)? = null,
        force: Boolean = false,
        token: String? = null
    ) = activity.lifecycle.coroutineScope.launch(Dispatchers.Main) {

        val currentVersionName = activity.getVersionName()

        val key = "LAST_VERSION_CHECK"
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

        if (force || prefs.getLong(key, 0) < System.currentTimeMillis() - 1000 * 3600 * 24 / 24 / 60 * 5) {
            try {
                val versionList = requestGithubVersions(gitRepoUrl, token)
                prefs.edit().putLong(key, System.currentTimeMillis()).apply()

                versionList.body()?.firstOrNull()?.let { release ->
                    val assetApk = release.assets.find { it.name.endsWith("release.apk") }

                    Timber.d(release.tagName + " > " + currentVersionName + " " + (release.tagName > currentVersionName))
                    if (release.tagName > currentVersionName) {
                        askUser(activity, currentVersionName, release, assetApk)
                    } else {
                        callback?.invoke("Nothing to do with ${release.tagName}")
                    }
                }
                if (versionList.code() != HttpURLConnection.HTTP_OK)
                    throw RuntimeException("call delivers ${versionList.code()}")
            } catch (e: Exception) {
                Timber.e("git check deliver: ${e.message}")
                Toast.makeText(activity, "git check delivers: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    internal fun checkForNewVersionSilent(
        appContext: Context,
        currentVersionName: String,
        gitRepoUrl: String,
        token: String? = null
    ){
        try {
            val versionList = requestVersionsSync(gitRepoUrl, token)
            Timber.e("currentVersionName $currentVersionName")
            versionList.body()?.firstOrNull()?.let { release ->
                val assetApk = release.assets.find { it.name.endsWith("release.apk") }

                Timber.d(release.tagName + " > " + currentVersionName + " " + (release.tagName > currentVersionName))
                val text = "You use version $currentVersionName\n" +
                        "and there is a new version ${release.tagName}\n"
                if (release.tagName > currentVersionName) {
                    Timber.w(text)
                    Notify.notification(appContext, text, "New version for '${getAppName(appContext)}'", assetApk, release)
                }
            }
            if (versionList.code() != HttpURLConnection.HTTP_OK)
                throw RuntimeException("call delivers ${versionList.code()}")
        } catch (e: Exception) {
            Timber.e("git check deliver: ${e.message}")
        }
    }

    private fun requestVersionsSync(gitRepoUrl: String, token: String? = null): Response<MutableList<GithubVersion>> {
        val client = GithubClient(HttpLoggingInterceptor.Level.BODY, token)
        return client.github.getGithubVersions(gitRepoUrl.user(), gitRepoUrl.repo()).execute()
    }

    private suspend fun requestGithubVersions(gitRepoUrl: String, token: String? = null): Response<MutableList<GithubVersion>> {
        val versionList = withContext(Dispatchers.Default) {
            val client = GithubClient(HttpLoggingInterceptor.Level.BODY, token)
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
            .setTitle(activity.getString(R.string.new_version))
            .setMessage(
                "'${getAppName(activity)}' use version \n$currentVersionName\n" +
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
                Timber.d("open $uriUrl")
                activity.startActivity(Intent(Intent.ACTION_VIEW, uriUrl))
                dialog.dismiss()
            }

        assetApk?.let {
            dialog.setNeutralButton(activity.getString(R.string.directDownload)) { dialog, _ ->
                val uriUrl = Uri.parse(it.browserDownloadUrl)
                Timber.d("open $uriUrl")
                activity.startActivity(Intent(Intent.ACTION_VIEW, uriUrl))
                dialog.dismiss()
            }
        }
        return dialog.show()
    }

    private fun getAppName(context: Context): CharSequence {
        val pm = context.applicationContext.packageManager
        val appInfo: ApplicationInfo = pm.getApplicationInfo(context.applicationContext.packageName, 0)
        return context.applicationContext.packageManager.getApplicationLabel(appInfo)
    }

}