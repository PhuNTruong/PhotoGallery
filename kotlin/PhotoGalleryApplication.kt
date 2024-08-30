package com.bignerdranch.android.photogallery

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build


//22.28
const val NOTIFICATION_CHANNEL_ID = "flickr_poll"

//Listing 21.5 Creating PhotoGalleryApplication
//create new class and have it extend the class
//initialize the PreferencesRepository in the onCreate() method
class PhotoGalleryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferencesRepository.initialize(this)

        //Listing 22.8 Creating a notification channel (since it is Oreo+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //we add strings for this in 22.9
            val name = getString(R.string.notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}