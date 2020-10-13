package com.bignerdranch.android.photogallery.api

import com.bignerdranch.android.photogallery.GalleryItem
import com.google.gson.annotations.SerializedName

/**
 * This Class Will map to the "photos" object in the JSON data
 * */

class PhotoResponse
{
    //Right now GSON will automaitcally create a list and populate it with gallery item objects based on the JSON array named "photo"
    @SerializedName("photo") lateinit var galleryItems: List<GalleryItem>
}