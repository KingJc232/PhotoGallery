package com.bignerdranch.android.photogallery.api

/**
 * This Class will map the outermost object
 * in the JSON data (The one at the top of the JSON object hierarchy, denoted by the outermost { })
 * */

class FlickrResponse
{
    lateinit var photos: PhotoResponse
}