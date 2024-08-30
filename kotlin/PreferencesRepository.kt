package com.bignerdranch.android.photogallery

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

//Listing 21.12 Accessing a stored query
class PreferencesRepository private constructor(
    //DataStore exposes it data through a coroutine flow
    private val dataStore: DataStore<Preferences>
) {
    //Exposing the stored query as a Flow<String> so that callers can easily acess the latest stored query
    //Map over the data property on DataStore<Preference> property, extracting the value for the key
    val storedQuery: Flow<String> = dataStore.data.map{
        it[SEARCH_QUERY_KEY] ?: ""
    }.distinctUntilChanged() //To prevent multiple emissions of the same value on the Flow, use the distinctUntilChanged() function

    //Listing 21.13 Setting a stored query
    suspend fun setStoredQuery(query: String) {
        dataStore.edit {
            it[SEARCH_QUERY_KEY] = query
        }
    }

    //Listing 22.5 Saving the latest photo ID
    //In order to add logic to check for new photos:
    //We need to save ID of most recent photo the user has seen
    //then update worker class to pull the new photos and compare stored id with newest one from server
    //Here we update PreferencesRepository to store and retrieve the latest photo ID from shared preferences
    val lastResultId: Flow<String> = dataStore.data.map {
        it[PREF_LAST_RESULT_ID] ?: ""
    }.distinctUntilChanged()

    suspend fun setLastResultId(lastResultId: String) {
        dataStore.edit {
            it[PREF_LAST_RESULT_ID] = lastResultId
        }
    }

    //Listing 22.12 Saving Worker state
    //We will update request to run periodically instead of once
    //We need to toggle worker for if it can run in the background or not
    //We need to check if its actually running
    //To do this m we supplemented PreferencesRepository to store a flag indicating whether the worker is enabled
    val isPolling: Flow<Boolean> = dataStore.data.map {
        it[PREF_IS_POLLING] ?: false
    }.distinctUntilChanged()

    suspend fun setPolling(isPolling: Boolean) {
        dataStore.edit {
            it[PREF_IS_POLLING] = isPolling
        }
    }

    companion object {
        private val SEARCH_QUERY_KEY = stringPreferencesKey("search_query")
        private val PREF_LAST_RESULT_ID = stringPreferencesKey("lastResultId")
        private val PREF_IS_POLLING = booleanPreferencesKey("isPolling")
        private var INSTANCE: PreferencesRepository? = null

        //Only piece you need to provide is the file where your data will be stored,
        //Do this by calling the preferencesDataStoreFile() extension function on the Context, passing in the name of that File
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                //Let the PreferenceDataStoreFactory class create the DataStore<Preferences> instance for you
                val dataStore = PreferenceDataStoreFactory.create {
                    context.preferencesDataStoreFile("settings")
                }

                INSTANCE = PreferencesRepository(dataStore)
            }
        }

        fun get(): PreferencesRepository {
            return INSTANCE ?: throw IllegalStateException(
                "PreferencesRepository must be initialized"
            )
        }
    }
}