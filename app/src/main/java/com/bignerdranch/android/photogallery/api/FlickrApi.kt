package com.bignerdranch.android.photogallery.api

import retrofit2.Call
import retrofit2.http.GET

/**
 * This is a RetroFit API Which is a Kotlin Interface that uses
 * Retrofit Annotations to Define API Calls
 * */
interface FlickrApi
{

    /**HTTP Request Method Annotation
     * @Get ("/") configurees the Call returned by fetchContents() to perform a GET request the "/" is the relative path
     * A Relative Path is a String representing the relative URL from the base URL of your API endpoint
     * By Default all retrofit web request return a retrofit2.Call obj
     * - A Call obj represents a single web request that you can execute
     * Executing A Call produces one corresponding web responds
     * The Type you use as Call's Generic Type parameter specifies the Data Type you would like Retrofit to
     * deserialize the HTTP response into
     * BY Default Retrofit Deserializes the response intoo an OkHttp.ResponseBody
     * Therefore Specifying Call<String> tells retrofit that you want the Response deserialized into a String Object instead
     *
     **/

/*    @GET("/")
    fun fetchContents(): Call<String>*/


    //Temporarly Hardcoding it
    @GET(
        "services/rest/?method=flickr.interestingness.getList" +
                "&api_key=6669f8c3002b6638f59deb2f2aa64a1b" +
                "&format=json" +
                "&nojsoncallback=1" +
                "&extras=url_s"
    )
    fun fetchPhotos(): Call<FlickrResponse>

}
