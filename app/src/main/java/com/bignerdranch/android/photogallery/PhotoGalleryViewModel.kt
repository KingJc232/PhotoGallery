package com.bignerdranch.android.photogallery

import android.app.Application
import android.app.DownloadManager
import androidx.lifecycle.*

/**Used to Store live data objects holding a list of gallery items
 * So that we can cache the Data and Not need to re download it every time the user rotations the device
 * */

class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app)
{
    val galleryItemLiveData: LiveData<List<GalleryItem>>

    //Used to search For the Photos in the Flickr Website
    private val flickrFetchr = FlickrFetchr()

    //Used to Keep track of Searchs the User does
    private val mutableSearchTerm = MutableLiveData<String>()

    private val queryPreferences: QueryPreferences = QueryPreferences()

    val searchTerm: String get() = mutableSearchTerm.value ?: ""

    //Kicking off the request for photo data to get grabbed from thee web
    //When ViewModel is first created
    init {

        mutableSearchTerm.value = queryPreferences.getStoredQuery(app)

        //Initializing the GalleryItemLiveData

        /**Since Both The Search Term and Gallery Item Lists are wrapped in LiveData
         * Using a Transformations.switchMap(trigger: LiveData<X>, transformFunction: Function<X, LiveData<Y>>) to implement this
         * Relationship
         * */
        this.galleryItemLiveData = Transformations.switchMap(mutableSearchTerm){
            searchTerm ->

                if(searchTerm.isBlank())
                {
                    flickrFetchr.fetchPhotos() //Will Fetch Interesting Photos If Search Is Initially Blank
                }
                else
                {
                    flickrFetchr.searchPhotos(searchTerm)
                }

        }

    }

    fun fetchPhotos(query: String = "")
    {
        queryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }


}