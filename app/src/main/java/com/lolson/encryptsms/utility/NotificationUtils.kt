package com.lolson.encryptsms.utility

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import com.lolson.encryptsms.MainActivity
import com.lolson.encryptsms.R

class NotificationUtils(_context: Context): ContextWrapper(_context)
{
    private val mContext = _context
    private var mManager: NotificationManager? = null

    // ID
    private val ANDROID_CHANNEL = "${packageName}-ANDROID"

    init
    {
        createChannel()
    }

    // Create a channel if SDK is O or greater
    private fun createChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val androidChannel = NotificationChannel(
                ANDROID_CHANNEL,
                "ANDROID_CHANNEL",
                NotificationManager.IMPORTANCE_DEFAULT)
            androidChannel.enableLights(true)
            androidChannel.enableVibration(true)
            androidChannel.lightColor = Color.GREEN
            getManager().createNotificationChannel(androidChannel)
        }
    }

    // Sets the channel for use by system
    fun getManager(
    ): NotificationManager
    {
        if (mManager == null)
        {
            mManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return mManager as NotificationManager
    }

    // Get the notification depending on SDK
    fun getAndroidNotifyChannel(
        title: String,
        body: String
    ):Notification.Builder
    {
        val intent = Intent(this, MainActivity::class.java ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra("address", title)
        intent.putExtra("offset", 800L)

        val pIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Notification.Builder(mContext, ANDROID_CHANNEL)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_message_black_24dp)
                .setContentIntent(pIntent)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setAutoCancel(true)
        }
        else
        {
            Notification.Builder(mContext)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_message_black_24dp)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
        }
    }
}