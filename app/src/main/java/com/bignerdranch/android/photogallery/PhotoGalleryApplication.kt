package com.bignerdranch.android.photogallery

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

const val NOTIFICATION_CHANNEL_ID = "flickr_poll"

/**Used to Create our Channel That is going to be used for notification */
class PhotoGalleryApplication : Application()
{
    override fun onCreate() {
        super.onCreate()

        /**If its using Android Oreo Or Higher need a Channel For Notifications */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val name = getString(R.string.notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            //Creating a Notification Channel
            notificationManager.createNotificationChannel(channel)
        }

    }
}