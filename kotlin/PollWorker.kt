package com.bignerdranch.android.photogallery

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first


//Listing 22.2 Creating the worker
private const val TAG = "PollWorker"

class PollWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        //Listing 22.6 Starting your work
        //getting the ids from the repositories using .first()
        val preferencesRepository = PreferencesRepository.get()
        val photoRepository = PhotoRepository()

        val query = preferencesRepository.storedQuery.first()
        val lastId = preferencesRepository.lastResultId.first()

        //If the user has not searched for anything yet,
        // you do not have a search term to look for new content. In that case, you can finish your work early.
        if (query.isEmpty()) {
            Log.i(TAG, "No saved query, finishing early.")
            return Result.success()
        }
        //Listing 22.7 Getting the work done
        //if there is stored query, you make request to get gallery items for that query
        //if it fails, have pollworker return result.failure
        //If network request succeeds, you check to see if it matches the one you have saved
        //whether or not the photo ids matchm you will return Result.success()
        return try {
            val items = photoRepository.searchPhotos(query)

            if (items.isNotEmpty()) {
                val newResultId = items.first().id
                if (newResultId == lastId) {
                    Log.i(TAG, "Still have the same result: $newResultId")
                } else {
                    Log.i(TAG, "Got a new result: $newResultId")
                    preferencesRepository.setLastResultId(newResultId)
                    //Listing 22.11 Adding a notification
                    notifyUser()
                }
            }

            Result.success()
        } catch (ex: Exception) {
            Log.e(TAG, "Background update failed", ex)
            Result.failure()
        }
    }

    //Listing 22.11 Adding a notification
    private fun notifyUser() {
        val intent = MainActivity.newIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val resources = context.resources

        //You use the NotificationCompat class to easily support notifications on both pre-Oreo and Oreo-and-above devices.

        val notification = NotificationCompat
            //NotificationCompat.Builder accepts a channel ID and
            //uses the ID to set the notification’s channel if the user is running Oreo or above.
            //If the user is running a pre-Oreo version of Android, NotificationCompat.Builder ignores the channel.
            //In Listing 22.8, you checked the build version SDK before creating channel, since there is no no AndroidX API for creating a channel.
            //You do not need to do that here, because NotificationCompat checks the build version for you
            .Builder(context, NOTIFICATION_CHANNEL_ID)
            //Next you configure the ticker text and small icon by calling setTicker(CharSequence) and setSmallIcon(Int)
            .setTicker(resources.getString(R.string.new_pictures_title))
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            //setContentTile and Text will set the title and text (CharSequence)
            .setContentTitle(resources.getString(R.string.new_pictures_title))
            .setContentText(resources.getString(R.string.new_pictures_text))
            //We specify what happens when user preses notification with pendingIntent
            .setContentIntent(pendingIntent)
            //Makes the notification closed when clicked
            .setAutoCancel(true)
            .build()

        //Call this to post notification
        NotificationManagerCompat.from(context).notify(0, notification)
    }
}