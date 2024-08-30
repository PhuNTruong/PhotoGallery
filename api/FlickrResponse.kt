package com.bignerdranch.android.photogallery.api

import com.squareup.moshi.JsonClass

//20.24 Adding FlickrResponse
//This class will map to the outermost object in the JSON data (the one at the top of the JSON object hierarchy, denoted by the outermost { }). Add a property to map to the "photos" field.
@JsonClass(generateAdapter = true)
data class FlickrResponse(
    val photos: PhotoResponse
)