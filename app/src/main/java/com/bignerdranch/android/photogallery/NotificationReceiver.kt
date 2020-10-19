package com.bignerdranch.android.photogallery

import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

private const val TAG = "NotificationReceiver"

/**Going to Be used to Receive BroadCast Intents
 *
 * A Broadcast receiver is a Component that receives intents, just like a service or an activity
 * When an Intent is issed to NotificationReceiver its onReceive() function will be called
 * */

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.i(TAG, "Received result: $resultCode")

        //A Foreground Activity Canceled the Broadcast
        if(resultCode != Activity.RESULT_OK)
            return

        val requestCode = p1?.getIntExtra(PollWorker.REQUEST_CODE, 0)
        val notification: Notification = p1?.getParcelableExtra(PollWorker.NOTIFICATION)!!

        val notificationManager = NotificationManagerCompat.from(p0!!)
        notificationManager.notify(requestCode!!, notification)
    }

}