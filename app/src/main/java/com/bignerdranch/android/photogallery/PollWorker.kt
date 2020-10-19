package com.bignerdranch.android.photogallery

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

private const val TAG = "PollWorker"

/**
 * The Worker Class is where you will put the logic you want to perform in the background
 *
 * Only knows how to execute the background work. You need another Component to schedule the work
 * */

class PollWorker(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams)
{

    private var queryPreferences: QueryPreferences = QueryPreferences()

    /**
     * The doWork() function is called from a background thread, so you can do any long running task you need in there
     *
     * What we are doing is pulling out the current search query and latest photo ID from QueryPreferences. If there is no
     * Search Query, Fetch the regular photos. If there is a serach query perform the search request. For Safety use an empty list if either
     * Request Fails to return any photos .
     * */
    override fun doWork(): Result
    {
        val query = this.queryPreferences.getStoredQuery(context)
        val lastResultId = this.queryPreferences.getStoredQuery(context)

        //If No Search Queries Fetch the Regular Photo Request
        val items: List<GalleryItem> = if(query.isEmpty()){
            FlickrFetchr().fetchPhotosRequest()
                .execute()
                .body()
                ?.photos
                ?.galleryItems
        }
        //Else If there is Search Queries Fetch them
        else {
            FlickrFetchr().searchPhotoRequest(query)
                .execute()
                .body()
                ?.photos
                ?.galleryItems
        } ?: emptyList()

        /**Checking to see Whether there are new Photos By Comparing The id  of the First item in the
         * list with the lastResultId property
         * */

        if(items.isEmpty())
        {
            return Result.success()
        }
        val resultId = items.first().id
        if(resultId == lastResultId)
        {
            Log.i(TAG, "Got an Old Result: $resultId")
        }
        else
        {
            Log.i(TAG, "Got a New Result: $resultId")
            this.queryPreferences.setLastResultId(context, resultId)

            /**Making PollWorker Notify the User that a New Result Is Ready By Creating A Notification And Calling NotificationManager.notify*/
            val intent = PhotoGalleryActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            val resources = context.resources

            //Creating a Notification With the Intent
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            //Calling Helper Method
            showBackgroundNotification(0,notification)
        }

        return Result.success()
    }

    /**
     * Behaves Like SendBroadcast(..) but it will guarantee that your broadcast is delievered
     * to each reciever one at a time The Result Code will be initially set to
     * Activity.RESULT_OK when this ordered broadcast is sent
     * */
    private fun showBackgroundNotification(requestCode: Int, notification: Notification)
    {

        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(REQUEST_CODE, requestCode)
            putExtra(NOTIFICATION, notification)
        }
        /**Sending a BroadCast Intent Notifying interested componenets that a new search results notification is ready to post */
        context.sendOrderedBroadcast(intent, PERM_PRIVATE)
    }


    companion object
    {
        const val ACTION_SHOW_NOTIFICATION =  "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION"
        const val PERM_PRIVATE = "com.bignerdranch.android.photogallery.PRIVATE"

        const val REQUEST_CODE = "REQUEST_CODE"
        const val NOTIFICATION = "NOTIFICATION"
    }


}




















