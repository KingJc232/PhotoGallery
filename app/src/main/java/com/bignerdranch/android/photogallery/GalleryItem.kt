package com.bignerdranch.android.photogallery


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
    var url: String = ""
)
{}