package info.hannes.github

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import info.hannes.github.model.Asset
import info.hannes.github.model.GithubVersion

internal object Notify {

    private var MessageID = 120
    private const val channelId = "chn-01"
    private const val channelFireBaseMsg = "Channel appUpdate"

    private val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    fun notification(context: Context, messageString: String, notificationTitle: String, assetApk: Asset?, release: GithubVersion) {

        //Get the notification manage which we will use to display the notification
        val ns = Context.NOTIFICATION_SERVICE
        val notificationManager = context.getSystemService(ns) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelFireBaseMsg, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        //get the notification title from the application's strings.xml file
        val contentTitle: CharSequence = notificationTitle

        //the message that will be displayed as the ticker
        val ticker = "$contentTitle $messageString"

        val showUrl = Uri.parse(release.htmlUrl)
        val showPendingIntent = PendingIntent.getActivity(context, 0, Intent(Intent.ACTION_VIEW, showUrl), pendingIntentFlags)

        //build the notification
        val notificationCompat = NotificationCompat.Builder(context, channelId)
            .setAutoCancel(true)
            .setContentTitle(contentTitle)
            .setContentIntent(showPendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageString))
            .setContentText(messageString)
            .setTicker(ticker)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.githib_logo)
            .addAction(R.drawable.githib_logo, context.getString(R.string.showRelease), showPendingIntent)

        assetApk?.let {
            val uriUrl = Uri.parse(it.browserDownloadUrl)
            val directPendingIntent = PendingIntent.getActivity(context, 0, Intent(Intent.ACTION_VIEW, uriUrl), pendingIntentFlags)
            notificationCompat.addAction(R.drawable.githib_logo, context.getString(R.string.directDownload), directPendingIntent)
        }

        val notification = notificationCompat.build()

        notificationManager.notify(MessageID, notification)
    }

}
