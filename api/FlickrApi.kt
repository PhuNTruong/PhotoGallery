package com.bignerdranch.android.photogallery.api

import retrofit2.http.GET
import retrofit2.http.Query

//20.16
//Listing 21.3
//removed flickr.interestingness.getList URL specified in FlickrAPI

//20.6 Adding a Retrofit API interface
interface FlickrApi {
    //Listing 20.16 Defining the "fetch recent interesting photos" request
    //20.25 Updating fetchPhoto()'s return type
    //Listing 21.3: Adding a search function to FlickrApi
    @GET("services/rest/?method=flickr.interestingness.getList")
    suspend fun fetchPhotos(): FlickrResponse

    @GET("services/rest?method=flickr.photos.search")
    suspend fun searchPhotos(@Query("text") query: String): FlickrResponse
}
//https://www.flickr.com/services/api/flickr.interestingness.getList.html
//services/rest/?method=flickr.interestingness.getList