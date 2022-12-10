package info.hannes.github

import android.content.Context
import androidx.work.*
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit

class DownloadWorker(private val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        // Get the input
        val currentVersion = inputData.getString(CURRENT_VERSION)!!
        val repoUrl = inputData.getString(REPO_URL)!!

        val d = async {
            check(currentVersion, repoUrl)
            "done"
        }
        val value = d.await()

        // output param is just a test
        val outputData = workDataOf(Pair("state", value))
        Result.success(outputData)
    }

    private fun check(currentVersion: String, repoUrl: String) {
        AppUpdateHelper.checkForNewVersionSilent(appContext, currentVersion, repoUrl)
    }

    companion object {
        fun run(context: Context, currentVersionName: String, repoUrl: String, repeatTime : Long, timeUnit: TimeUnit): ListenableFuture<MutableList<WorkInfo>> {
            val data = workDataOf(
                Pair(CURRENT_VERSION, currentVersionName),
                Pair(REPO_URL, repoUrl)
            )

            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresStorageNotLow(true)
                .build()

            val periodicWorkRequest = PeriodicWorkRequest
                .Builder(DownloadWorker::class.java, repeatTime, timeUnit)
                .setConstraints(constraints)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context)
                .cancelUniqueWork(uniqueWorkName)
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(uniqueWorkName, ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest)

            return WorkManager.getInstance(context).getWorkInfosForUniqueWork(uniqueWorkName)
        }

        const val CURRENT_VERSION = "CURRENT_VERSION"
        const val REPO_URL = "REPO_URL"
        private const val uniqueWorkName = "PWD"
    }
}