package com.bignerdranch.android.photogallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.databinding.FragmentPhotoGalleryBinding
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit


//Listing 20.10 Making a network request
private const val TAG = "PhotoGalleryFragment"

//22.19
private const val POLL_WORK = "POLL_WORK"

//Listing 20.3
//We subclass the fragment class, then inflate and brind layout using view binding
class PhotoGalleryFragment : Fragment() {
    private var _binding: FragmentPhotoGalleryBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    //Listing 21.21 Holding a reference to your SearchView
    //We don't want to hold a reference to search view for longer than necessary
    //null normally
    private var searchView: SearchView? = null
    //Listing 22.15 Accessing the Menu Item
    private var pollingMenuItem: MenuItem? = null


    //Listing 20.29 Getting a ViewModel instance from the provider
    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()

    //Listing 21.8 Overriding onCreateOptionsMenu(...)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        //Listing 22.3 Scheduling a WorkRequest
        //Listing 22.4 Adding work constraints (androidx.work.Constraints)
        //constraint is unmetered network
        //22.19 Handling poll-toggling item clicks, we remove OneTimeWorkRequest

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentPhotoGalleryBinding.inflate(inflater, container, false)
        binding.photoGrid.layoutManager = GridLayoutManager(context, 3)
        return binding.root
    }

    //20.7 Using the Retrofit object to create an instance of the API
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //20.13 Cutting Retrofit setup from the fragment
        //We do this because we put retrofit in PhotoRepository

        //Listing 20.10 Making a network request
        //launch coroutine using lifescycle property, then call fetchContents(), log results
        viewLifecycleOwner.lifecycleScope.launch {
            //Listing 20.27 Handling network errors
            //20.29 getting a ViewModel instance from the provider, removed code from 20.27
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                //Listing 21.22 Pre-populating SearchView
                photoGalleryViewModel.uiState.collect { state ->
                    binding.photoGrid.adapter = PhotoListAdapter(
                        state.images
                    ) { photoPageUri ->
                        findNavController().navigate(
                            PhotoGalleryFragmentDirections.showPhoto(photoPageUri)
                        )
                    }
                    searchView?.setQuery(state.query, false)
                    //22.17 Setting correct menu item text
                    updatePollingState(state.isPolling)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //21.8
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        //Listing 21.10 Logging SearchView.OnQueryTextListener
        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        //21.21 removed val before val searchView
        searchView = searchItem.actionView as? SearchView

        //Listing 22.15
        pollingMenuItem = menu.findItem(R.id.menu_item_toggle_polling)

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(TAG, "QueryTextSubmit: $query")
                photoGalleryViewModel.setQuery(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "QueryTextChange: $newText")
                return false
            }
        })
    }

    //Listing 21.18 Clearing a stored query
    //setting it to "" when user selects the Clear Search Item from the overflow menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.setQuery("")
                true
            }
            //Listing 22.18 Handling menu item presses
            //call toggleIsPolling() on ViewModel whenever menu item is pressed
            R.id.menu_item_toggle_polling -> {
                photoGalleryViewModel.toggleIsPolling()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //21.21 this will dereference the searchView when onDestroyOptionsMenu is used
    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        searchView = null
        //22.15
        pollingMenuItem = null
    }

    //22.17
    //Update meny item whenever a new PhotoGalleryUiState value is received
    private fun updatePollingState(isPolling: Boolean) {
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        pollingMenuItem?.setTitle(toggleItemTitle)

        //22.19 Handling poll-toggling item clicks
        //update the background work. If the worker is not running,
        //create a new PeriodicWorkRequest and schedule it with the WorkManager.
        //If the worker is running, stop it.
        if (isPolling) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
            val periodicRequest =
                PeriodicWorkRequestBuilder<PollWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()
            WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                POLL_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        } else {
            WorkManager.getInstance(requireContext()).cancelUniqueWork(POLL_WORK)
        }
    }
}