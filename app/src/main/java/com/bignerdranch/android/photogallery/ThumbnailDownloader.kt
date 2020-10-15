package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"


//Used as the "what" when sending Messages from this Background Thread To the Main Thread
private const val MESSAGE_DOWNLOAD = 0


/**This will be the Dedicated Background Thread That will receive and process request one at a time*/

/**
 * This class will be Lifecycle Aware meaning that it will create when the Hosting Activity Or Fragment Is created
 * And will be destroyed when the hosting Activity or Fragment is Destroyed
 * */

//Making it a Generic Class
class ThumbnailDownloader<in T>(
    //Used to Hold a Handler passed from the Main Thread
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap) -> Unit
) : HandlerThread(TAG)
{
    private var hasQuit = false

    //Defining a Handler to Handle the Transferring of Messages from this Background Thread to the Main Thread
    //Responsible for Queueing Download Requests as messages onto the ThumbnailDownloader Background Thread
    private lateinit var requestHandler: Handler

    //A ThreadSafe Version Of HashMap Used to Store and retrieve the URL associated with a particular request
    //So The request response can be easily routed back to the UI element where the downloaded image should be placed
    private val requestMap = ConcurrentHashMap<T , String>()

    //Stores a reference to a FlickrFetchr Instance This way all the Retrofit setup Code will only execute once
    //During the LifeTime of the thread (Since Multiple setups can slow Our App Down)
    private val flickrFetchr = FlickrFetchr()


    /**Refactoring Lifecycle Implementation to Make room for a second lifecycle observer implementation*/
    val fragmentLifeCycleObserver: LifecycleObserver =
        object : LifecycleObserver{
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun setup()
            {
                Log.i(TAG, "Starting Background Thread")
                //Its going to start itself when PhotoGalleryFragment onCreate(...) Is called since its observing its lifecycle
                start()
                looper
            }

            /**This Will Destroy the Background Thread when the hosting Fragment or Activity is Destroyed*/
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun tearDown()
            {
                Log.i(TAG, "Destroying Background Thread")
                //Its going to quit itself when PhotoGalleryFragment onDestroy(..) Is Called since its observing its lifecycle
                quit()
            }
        }

    /**Defining A New Observer that will Listen to lifecycle callbacks from the Fragments View
     *
     * Since App Could have Crashed if user Rotates the Screen And ThumbnailDownloader might be handing to invalid PhotoHolders
     * We will clear all Request out of the queue when the Fragments view is destroyed
     * */
    val viewLifecycleObserver: LifecycleObserver = object : LifecycleObserver{
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun clearQueue()
        {
            Log.i(TAG, "Clearing All request from queue")
            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
            requestMap.clear()
        }

    }


    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        //Good place to Initialize our Request Handler since this is called before the Looper checks the queue for the first time
        //Defining our Handler Sub Class using an Anynomous Inline class
        requestHandler = object : Handler() {
            /**Handler.handleMessage(..) will get called when a download message is pulled off the queue and ready to be processed
             * */
            override fun handleMessage(msg: Message) {
                //Ensuring the Message type is MESSAGE_DOWNLOAD
                //If so then pass the handle request
                if (msg.what == MESSAGE_DOWNLOAD){
                    val target = msg.obj as T
                    Log.i(TAG, "Got a Request for URL: ${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }

    /**Helper Function where the Downloading happens
     * First checks for existence of URL Then Passing URL To FlickrFectchr.getchPhoto(...)
     * */
    private fun handleRequest(target: T)
    {
        val url = requestMap[target] ?: return
        val bitmap = flickrFetchr.fetchPhoto(url) ?: return

        /**Posting a Runnable to the Main Thread's Queue Through responseHandler
         *
         * Because ResponseHandler is associated with the main thread's looper all the code inside of
         * Runnable's run() will be executed on the main thread
         *
         * First It Double checks the requestMap Because RecyclerView Recycles its views
         * Ensuring that PhotoHolder gets the correct image even if another request has been made in the meantime
         * Finally You remove the PhotoHolder-URL mapping from the requestMap and set the bitmap on the target PhotoHolder
         * */
        this.responseHandler.post(Runnable{
            if(requestMap[target] != url || hasQuit)
            {
                return@Runnable
            }
            requestMap.remove(target)
            onThumbnailDownloaded(target, bitmap)
        })

    }


    //Overriding the Quit Method of the Thread
    override fun quit(): Boolean
    {
        this.hasQuit = true
        return super.quit()
    }




    /**This function expects an object of type T to use as the identifier for the download and a String Containing the URL to download
     *
     * Used So that the background thread (this thread) can Communicate with the Main Thread
     * Using Handlers = (Think of Deliever people) Loopers = (Think of Mail box)
     * This is the function we will have PhotoAdapter Call in its onBindViewHolder(...) Implementation
     *
     * Going to Obtain a Message and Send it to its target
     *
     * */
    fun queueThumbnail(target: T, url: String)
    {
        Log.i(TAG, "Got a URL: $url")

        //Saving it
        this.requestMap[target] = url
        //Obtaining the Message directly which automatically sets the new Message Object's target field to requestHandler
        this.requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }


}