package com.bignerdranch.android.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

/**Used to Store live data objects holding a list of gallery items
 * So that we can cache the Data and Not need to re download it every time the user rotations the device
 * */

class PhotoGalleryViewModel : ViewModel()
{
    val galleryItemLiveData: LiveData<List<GalleryItem>>

    //Kicking off the request for photo data to get grabbed from thee web
    //When ViewModel is first created
    init {
        //Initializing the GalleryItemLiveData
        this.galleryItemLiveData = FlickrFetchr().fetchPhotos()
    }

}