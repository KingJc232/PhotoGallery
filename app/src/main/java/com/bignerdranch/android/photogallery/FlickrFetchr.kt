package com.bignerdranch.android.photogallery

import android.app.DownloadManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.api.FlickrResponse
import com.bignerdranch.android.photogallery.api.PhotoInterceptor
import com.bignerdranch.android.photogallery.api.PhotoResponse
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


/**To get more of A Repository Pattern
 * We moved it from our Fragment Class to here
 * */

//Tag used to Execute the Web Request In the Call Object flickrHomePageRequest
private const val TAG = "PhotoGalleryFragment"


/**FlickrFetchr Will Wrap Most Of the Networking Code in photo Gallery
 *
 * Updating FlickrFetchr To Expose the Retrofit Call objects for OUR Worker To Use
 * */
class FlickrFetchr {


    private val flickrApi: FlickrApi


    init {

        /**Creating a OkHttpClient Instance and adding PhotoInterceptor as an Interceptor*/
        val client = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor())
            .build()

        /**
         * Creating a Retrofit Object So that I can make web request based on the API Interface (FlickrApi) I defined
         * */
        /**
         * Notice How Retrofit.Builder() is a Fluent Interface therefore we were able to define the baseURL and build it
         * Note: Retrofit Does Not Generate code at Compile time instead it does all the work at runtime
         * Also Adding a Scalars Converter so that Retrofit can convert OkHttp Objects to Strings Since we return one in our Custom API
         * */

        /**Since we added our New Client Which includes our Interceptor .client(client)
         * Anytime A Request is Made PhotoInterceptor.intercept will be applied to that request
         * */

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
                //Now using a Gson Converter Instead of a Scalar Converter
            .addConverterFactory(GsonConverterFactory.create())
            .client(client) //Adding the Newly Configured client on our Retrofit Instance
            .build()


        /**     Using our Retrofit Object To Create a Instance of OUR INTERFACE FlickrApi
        When You call retrofit.create() Retrofit uses the information in the API interface to create and instantiate an anonymous class
        That implements our interface on the fly*/
        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    //Exposing the Call Objects to add the queries to PollWorker
    fun fetchPhotosRequest() :Call<FlickrResponse>
    {
        return flickrApi.fetchPhotos()
    }


    fun fetchPhotos(): LiveData<List<GalleryItem>>
    {
        /**Now we are going to execute a web request and log the results
         * Using the method we defined in the Interface fetchContents (RetroFit Will create the implementation and return the request
         * As well as convert it to a string type since we are using Squares Scalar Converter
         * */

        return fetchPhotoMetadata(fetchPhotosRequest())
    }
    //Exposing the Call Object
    fun searchPhotoRequest(query: String) : Call<FlickrResponse>
    {
        return flickrApi.searchPhotos(query)
    }

    fun searchPhotos(query: String) : LiveData<List<GalleryItem>>
    {
        return fetchPhotoMetadata(searchPhotoRequest(query))
    }



    /**Adding a function that fetches the Bytes from a Given URL and decodes them into a Bitmap*/

    //@WorkerThread Indicates that this function should only be called on a background thread
    //However the annotation itself does not take care of making a thread or putting the work an a background Thread
    @WorkerThread
    fun fetchPhoto(url: String): Bitmap?
    {
        //Call.execute() executes the Web request Synchronously (Since we know Networking on the Main Thread is Not Allowed)
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }


    /**Enqueues The Network Request and wraps the result in LiveData */
    private fun fetchPhotoMetadata(flickrRequest: Call<FlickrResponse>) : LiveData<List<GalleryItem>>
    {
        //Used to Save the response.body()
        /**Which when the Running Request on the Background Thread is Complete ,
         * Retrofit will store it in response.body() */
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()

        /**Using a Kotlin anonymous Class To determine when a Failure or a response was received using Log class
         * Call.enqueue() executes the web request represented by the Call object.
         * It Executes the request on a background thread
         * */
        flickrRequest.enqueue(object : Callback<FlickrResponse> {
            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG,"Failed To Fetch Photos", t)
            }

            override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
                Log.d(TAG, "Response Received: ${response.body()}")

                val flickrResponse: FlickrResponse? = response.body()

                val photoResponse: PhotoResponse? = flickrResponse?.photos

                //Fliters out gallery items with blank URL values using filter Not
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?:
                        mutableListOf()
                    galleryItems = galleryItems.filterNot {
                        it.url.isBlank()
                    }
                responseLiveData.value = galleryItems
            }
        })

        return responseLiveData //Returning the Live Data

    }





}






















