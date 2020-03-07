package com.crazy_iter.eresta

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat

object NewsNotifications {

    private val NOTIFICATION_TAG = "News"
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private val channelID = "com.cazy_iter.olxidea"
    private val description = "OLX Idea"

    fun notify(context: Context, number: Int, title: String, body: String, intent: Intent? = null) {

        val res = context.resources
        notificationManager = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelID, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

            val builder = NotificationCompat.Builder(context, channelID)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(body).setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setTicker(context.getString(R.string.app_name))
                    .setNumber(number)
//                    .addAction(
//                            R.drawable.ic_done_all,
//                            res.getString(R.string.action_read),
//                            PendingIntent.getActivity(
//                                    context,
//                                    0,
//                                    Intent.createChooser(Intent(Intent.ACTION_SEND)
//                                            .setType("text/plain")
//                                            .putExtra(Intent.EXTRA_TEXT, "Dummy text"), "Dummy title"),
//                                    PendingIntent.FLAG_UPDATE_CURRENT))


                    .setStyle(NotificationCompat.BigTextStyle().bigText(body)
                            .setBigContentTitle(title)
                            .setSummaryText(title))
                    .setAutoCancel(true)

            if (intent != null) {
                builder.setContentIntent(
                        PendingIntent.getActivity(context, 0, intent,
                                PendingIntent.FLAG_UPDATE_CURRENT))
            }

            notificationManager.notify(number, builder.build())

        } else {
            val builder = NotificationCompat.Builder(context)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(body).setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setTicker(context.getString(R.string.app_name))
                    .setNumber(number)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body)
                            .setBigContentTitle(title)
                            .setSummaryText(title))
                    .setAutoCancel(true)

            if (intent != null) {
                builder.setContentIntent(
                        PendingIntent.getActivity(context, 0, intent,
                                PendingIntent.FLAG_UPDATE_CURRENT))
            }

            notificationManager.notify(number, builder.build())

        }

     }

}
