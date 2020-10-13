package com.bignerdranch.android.photogallery

import com.google.gson.annotations.SerializedName


/**Model Class used to Model JSON Data We are getting from Flickr It will have
 *
 * Meta Info for a single photo
 * Title,
 * ID,
 * URL To download the image from 
 * */

data class GalleryItem(
    var title: String = "",
    var id: String = "",
    /**Since we want to keep this name and JSon uses "Url_s" we will tell it to map it to this one */
    @SerializedName("url_s") var url: String = ""
)
{}