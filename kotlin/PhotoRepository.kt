package com.bignerdranch.android.photogallery

import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.api.GalleryItem
import com.bignerdranch.android.photogallery.api.PhotoInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

//LListing 20.11 Creating PhotoRepository
//split flickrApi declaration and assignment onto 2 lines to declare flickrAPI as private property on PhotoRepositry
//This will allow you to access it elsewhere in the class (outside init block) but not outside the class
class PhotoRepository {
    private val flickrApi: FlickrApi

    init {
        //Listing 21.2 Adding an interceptor to your Retrofit configuration
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor())
            .build()

        val retrofit: Retrofit = Retrofit.Builder()
            //20.17 Updating the base URL
            .baseUrl("https://api.flickr.com/")
            //20.26 Updating PhotoRepository for Moshi (removed scalars factory)
            .addConverterFactory(MoshiConverterFactory.create())
            //21.2
            .client(okHttpClient)
            .build()
        flickrApi = retrofit.create()
    }

    //20.14 Adding fetchContents() to PhotoRepository
    //20.17 Updating the base URL
    //20.26 Updating PhotoRepository for Moshi
    suspend fun fetchPhotos(): List<GalleryItem> =
        flickrApi.fetchPhotos().photos.galleryItems

    //Listing 21.4 Adding a search function to PhotoRepository
    suspend fun searchPhotos(query: String): List<GalleryItem> =
        flickrApi.searchPhotos(query).photos.galleryItems
}