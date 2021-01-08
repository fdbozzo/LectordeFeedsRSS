package com.blogspot.fdbozzo.lectorfeedsrss.ui.main

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.blogspot.fdbozzo.lectorfeedsrss.MainSharedViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.BottomSheetFeedOptionsMenuFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.ui.SealedClassAppScreens
import com.blogspot.fdbozzo.lectorfeedsrss.ui.feed.FeedChannelFragment
import com.blogspot.fdbozzo.lectorfeedsrss.util.toBoolean
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import timber.log.Timber

class BottomSheetFeedOptionsMenuFragment(val tituloMenu: String, val feed: DomainFeed): BottomSheetDialogFragment() {

    private var _binding: BottomSheetFeedOptionsMenuFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mainSharedViewModel: MainSharedViewModel
    var addFeedToFavoritesVisibility = View.GONE
    var removeFeedFromFavoritesVisibility = View.GONE


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetFeedOptionsMenuFragmentBinding.inflate(inflater, container, false)
        Timber.d("[Timber] BottomSheetFeedOptionsMenuFragment.onCreateView() -- BOTTOM SHEET (%s) -> Feed encontrado: %s, favorite=%d",
            tituloMenu, feed, feed.favorite)

        val localDatabase = FeedDatabase.getInstance(requireContext())
        val feedRepository = FeedRepository(RoomDataSource(localDatabase), RssFeedDataSource())
        val sharedViewModel: MainSharedViewModel by activityViewModels { MainSharedViewModel.Factory(feedRepository) }
        mainSharedViewModel = sharedViewModel
        //mainSharedViewModel.setActiveScreen(SealedClassAppScreens.FeedChannelFragment)
        binding.lifecycleOwner = this // Para que LiveData sea consciente del LifeCycle y se actualice la uI

        mainSharedViewModel.tituloMenuFeed = tituloMenu

        if (feed.favorite.toBoolean()) {
            addFeedToFavoritesVisibility = View.GONE
            removeFeedFromFavoritesVisibility = View.VISIBLE
        } else {
            addFeedToFavoritesVisibility = View.VISIBLE
            removeFeedFromFavoritesVisibility = View.GONE
        }

        Timber.d("[Timber] BOTTOM SHEET -> addFeedToFavoritesVisibility: %s",
            if (addFeedToFavoritesVisibility == View.VISIBLE) "VISIBLE" else "GONE")
        Timber.d("[Timber] BOTTOM SHEET -> removeFeedFromFavoritesVisibility: %s",
            if (removeFeedFromFavoritesVisibility == View.VISIBLE) "VISIBLE" else "GONE")

        binding.viewModel = mainSharedViewModel
        binding.txtTituloMenu.text = tituloMenu
        binding.fragment = this

        return binding.root
        //return inflater.inflate(R.layout.bottom_sheet_group_options_menu_fragment, container, false)
    }

    fun updateMarkFeedAsReadFromBottomSheetFeedMenu(titulo: String) {
        Timber.d("[Timber] (BottomSheetFragment) updateMarkFeedAsReadFromBottomSheetFeedMenu(%s)", titulo)
        mainSharedViewModel.updateMarkFeedAsRead(titulo)
        dismiss()
    }

    fun removeFeedFromFavorites(titulo: String) {
        Timber.d("[Timber] (BottomSheetFragment) removeFeedFromFavorites(%s)", titulo)
        mainSharedViewModel.updateFeedFavoriteState(titulo, false)
        dismiss()
    }

    fun addFeedToFavorites(titulo: String) {
        Timber.d("[Timber] (BottomSheetFragment) addFeedToFavorites(%s)", titulo)
        mainSharedViewModel.updateFeedFavoriteState(titulo, true)
        dismiss()
    }

    fun unfollowFeed(titulo: String) {
        Timber.d("[Timber] (BottomSheetFragment) unfollowFeed(%s)", titulo)
        mainSharedViewModel.deleteFeed(titulo)
        dismiss()
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*
        val options = listOf<String>(
            "Share with Friends",
            "Bookmark",
            "Add to Favourites",
            "More Information"
        )

         */


        /*
        listViewOptions.adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            options
        )
         */
    }
}