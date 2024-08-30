package com.bignerdranch.android.photogallery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.photogallery.api.GalleryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//Listing 20.28 Shiny new ViewModel
//Use StateFlow to expose a list of gallery items to the fragment
//Start web request to fetch photo data when the ViewModel is first initialized
//Stash the resulting data in the property you created
//Try/catch block for handling errors
private const val TAG = "PhotoGalleryViewModel"

class PhotoGalleryViewModel : ViewModel() {
    private val photoRepository = PhotoRepository()
    //Listing 21.17 Persisting the query
    private val preferencesRepository = PreferencesRepository.get()

    //Listing 21.20 Exposing the search term from PhotoGalleryViewModel
    //We updated the ViewModel to expose PhotoGalleryUiState instead of <List<GalleryItem>>
    private val _uiState: MutableStateFlow<PhotoGalleryUiState> =
        MutableStateFlow(PhotoGalleryUiState())
    val uiState: StateFlow<PhotoGalleryUiState>
        get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            //21.17
            preferencesRepository.storedQuery.collectLatest { storedQuery ->
                try {
                    //Listing 21.5 Kicking off a search request
                    //hard coded to test planets and removed log
                    val items = fetchGalleryItems(storedQuery)
                    //Listing 21.20
                    _uiState.update { oldState ->
                        oldState.copy(
                            images = items,
                            query = storedQuery
                        )
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "Failed to fetch gallery items", ex)
                }
            }
        }

        //listing 22.16 Adding more data to PhotoGalleryUiState
        viewModelScope.launch {
            preferencesRepository.isPolling.collect { isPolling ->
                _uiState.update { it.copy(isPolling = isPolling) }
            }
        }

    }


    //Listing 21.9 Searching in PhotoGalleryViewModel
    //Put it into own private function since code to make network request is appearing in 2 locations
    fun setQuery(query: String) {
        viewModelScope.launch { preferencesRepository.setStoredQuery(query) }
    }

    //listing 22.16
    fun toggleIsPolling() {
        viewModelScope.launch {
            preferencesRepository.setPolling(!uiState.value.isPolling)
        }
    }

    private suspend fun fetchGalleryItems(query: String): List<GalleryItem> {
        return if (query.isNotEmpty()) {
            photoRepository.searchPhotos(query)
        } else {
            photoRepository.fetchPhotos()
        }
    }
}

//Listing 21.19 Creating PhotoGalleryUIState
//Combine the list of photos and the search query into a single value that get sents to PhotoGalleryFragment
//We can do that by defining a new data class to track UI State
//2 pieces of data are list of gallery items and value in search text box
data class PhotoGalleryUiState(
    val images: List<GalleryItem> = listOf(),
    val query: String = "",
    //22.16
    val isPolling: Boolean = false,
)