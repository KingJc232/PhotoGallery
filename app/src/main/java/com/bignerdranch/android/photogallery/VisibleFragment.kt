package com.bignerdranch.android.photogallery

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment


private const val TAG = "VisibleFragment"

/**Defining Our Dynamic Receiver in a Generic Fragment class to hide foreground Notifications
 *
 * Wrapping it in a Fragment class to use its lifecycle functions to register and unregister the receiver (Reuses Code)
 *
 * */
abstract class VisibleFragment : Fragment()
{
    /**Creating our own Dynamic Receiver Class since we will use this ALOT*/
    private val onShowNotification = (object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?)
        {

            //If We Receive this we're visible therefore cancel the notification
            Log.i(TAG, "Canceling Notification")
            resultCode = Activity.RESULT_CANCELED
        }
    })

    /**Registering The Dynamic Receiver When Creating the Fragment */
    override fun onStart() {
        super.onStart()

        val filter = IntentFilter(PollWorker.ACTION_SHOW_NOTIFICATION)
        requireActivity().registerReceiver(
            onShowNotification,
            filter,
            PollWorker.PERM_PRIVATE,
            null
        )
    }
    /**Un Registering The Dynamic Receiver When the Fragment is Done*/
    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(onShowNotification)
    }

}