package com.bignerdranch.android.photogallery.api

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


//Saving My API Key
private const val API_KEY = "6669f8c3002b6638f59deb2f2aa64a1b"

/**
 * Going to Be used to Intercept A request or a response
 * */

class PhotoInterceptor : Interceptor
{

    //Intercepting a Request and Returning the Response
    override fun intercept(chain: Interceptor.Chain): Response {

        //Accesses the Original Request
        val originalRequest: Request = chain.request()

        //originalRequest.url() pulls the original URL from the request
        //The HttpUrl.Builder() allows us to add new query parameters to it
        val newUrl: HttpUrl = originalRequest.url().newBuilder()
            .addQueryParameter("api_key", API_KEY)
            .addQueryParameter("format","json")
            .addQueryParameter("nojsoncallback", "1")
            .addQueryParameter("extras","url_s")
            .addQueryParameter("safeSearch", "1")
            .build()
        //HttpUrl.Builder creates a new Request based on the original request and overwrites the original URL with the new one
        val newRequest: Request = originalRequest.newBuilder()
            .url(newUrl)
            .build()
        //Calling chain.proceed(newRequest) to produce a Response
        //If we do not call chain.proceed(..) the network request would not happen
        return chain.proceed(newRequest)
    }

}