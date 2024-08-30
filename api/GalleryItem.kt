package com.bignerdranch.android.photogallery.api

import android.net.Uri
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//20.22 Integrating Moshi
// To generate the code to adapt the JSON string into a GalleryItem,
//you need to anotate the class with the @JsonClass annotation
@JsonClass(generateAdapter = true)
//20.19 Creating a model object class
data class GalleryItem(
    val title: String,
    val id: String,
    //20.22 integrating moshi, adding in @Json
    @Json(name = "url_s") val url: String,

    //Listing 23.1 Adding code for the photo page
    val owner: String
) {
    val photoPageUri: Uri
        get() = Uri.parse("https://www.flickr.com/photos/")
            .buildUpon()
            .appendPath(owner)
            .appendPath(id)
            .build()
}
