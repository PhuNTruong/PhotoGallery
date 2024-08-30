package com.bignerdranch.android.photogallery.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//20.23 Adding PhotoResponse
//create a PhotoResponse class to map to the "photos" object in the JSON data.
//Include a property called galleryItems to store a list of gallery items and annotate it with @Json(name = "photo").
//Moshi will automatically create a list and populate it with gallery item objects based on the JSON array named "photo".
@JsonClass(generateAdapter = true)
data class PhotoResponse(
    @Json(name = "photo") val galleryItems: List<GalleryItem>
)