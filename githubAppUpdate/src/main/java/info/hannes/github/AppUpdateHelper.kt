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
import androidx.work.*
import info.hannes.github.model.Asset
import info.hannes.github.model.GithubVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.TimeUnit


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

    // silently in background
    fun checkForNewVersion(activity: AppCompatActivity, gitRepoUrl: String, repeatTime : Long = 6, timeUnit: TimeUnit = TimeUnit.HOURS) {
        val currentVersionName = activity.getVersionName()
        DownloadWorker.run(activity, currentVersionName, gitRepoUrl, repeatTime, timeUnit)
    }

    // with user feedback
    fun checkWithDialog(
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

                    Timber.d(release.tagName + " > " + currentVersionName + " " + (release.tagName > currentVersionName))
                    callback?.invoke(release.tagName)
                    if (release.tagName > currentVersionName) {
                        askUser(activity, currentVersionName, release, assetApk)
                    } else {
                        callback?.invoke("Nothing to do with ${release.tagName}")
                    }
                }
            } catch (e: Exception) {
                Timber.e("git check deliver: ${e.message}")
                Toast.makeText(activity, "git check delivers: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    internal suspend fun checkForNewVersionSilent(
        appContext: Context,
        currentVersionName: String,
        gitRepoUrl: String
    ){
        try {
            val versionList = requestVersionsSync(gitRepoUrl)
            Timber.e("currentVersionName $currentVersionName")
            versionList.body()?.firstOrNull()?.let { release ->
                val assetApk = release.assets.find { it.name.endsWith("release.apk") }

                Timber.d(release.tagName + " > " + currentVersionName + " " + (release.tagName > currentVersionName))
                val text = "You use version $currentVersionName\n" +
                        "and there is a new version ${release.tagName}\n"
                if (release.tagName > currentVersionName) {
                    Timber.w(text)
                    withContext(Dispatchers.Main) {
                        Notify.notification(appContext, text, "New version for '${getAppName(appContext)}'", assetApk, release)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("git check deliver: ${e.message}")
        }
    }

    private fun requestVersionsSync(gitRepoUrl: String): Response<MutableList<GithubVersion>> {
        val client = GithubClient(HttpLoggingInterceptor.Level.BODY)
        return client.github.getGithubVersions(gitRepoUrl.user(), gitRepoUrl.repo()).execute()
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
            .setTitle("New version for ${getAppName(activity)}")
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