package com.bignerdranch.android.photogallery

import android.net.Uri
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
    @SerializedName("url_s") var url: String = "" ,

    /**Creating a New Owner Property */
    @SerializedName("owner") var owner: String = ""
)
{

    /**Building the URL of the photo from exsiting JSON data (owner attribute and photo id)*/
    val photoPageUri : Uri
        get() {
            return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build()
        }


}