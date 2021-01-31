package com.blogspot.fdbozzo.lectorfeedsrss.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.blogspot.fdbozzo.lectorfeedsrss.MainSharedViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.BottomSheetFeedOptionsMenuFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.util.toBoolean
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber

class BottomSheetFeedOptionsMenuFragment(val tituloMenu: String, val feed: DomainFeed): BottomSheetDialogFragment() {

    private var _binding: BottomSheetFeedOptionsMenuFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mainSharedViewModel: MainSharedViewModel
    private var _addFeedToFavoritesVisibility = View.GONE
    val addFeedToFavoritesVisibility get() = _addFeedToFavoritesVisibility
    private var _removeFeedFromFavoritesVisibility = View.GONE
    val removeFeedFromFavoritesVisibility get() = _removeFeedFromFavoritesVisibility

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
        //mainSharedViewModel.setActiveScreen(SealedClassAppScreens.ChannelFragment)
        binding.lifecycleOwner = this // Para que LiveData sea consciente del LifeCycle y se actualice la uI

        mainSharedViewModel.tituloMenuFeed = tituloMenu

        if (feed.favorite.toBoolean()) {
            _addFeedToFavoritesVisibility = View.GONE
            _removeFeedFromFavoritesVisibility = View.VISIBLE
        } else {
            _addFeedToFavoritesVisibility = View.VISIBLE
            _removeFeedFromFavoritesVisibility = View.GONE
        }

        Timber.d("[Timber] BOTTOM SHEET -> addFeedToFavoritesVisibility: %s",
            if (_addFeedToFavoritesVisibility == View.VISIBLE) "VISIBLE" else "GONE")
        Timber.d("[Timber] BOTTOM SHEET -> removeFeedFromFavoritesVisibility: %s",
            if (_removeFeedFromFavoritesVisibility == View.VISIBLE) "VISIBLE" else "GONE")

        binding.viewModel = mainSharedViewModel
        binding.txtTituloMenu.text = tituloMenu
        binding.fragment = this

        return binding.root
        //return inflater.inflate(R.layout.bottom_sheet_group_options_menu_fragment, container, false)
    }

    fun updateMarkFeedAsReadFromBottomSheetFeedMenu(titulo: String) {
        try {
            Timber.d("[Timber] (BottomSheetFeedFragment) updateMarkFeedAsReadFromBottomSheetFeedMenu(%s)", titulo)
            mainSharedViewModel.updateMarkFeedAsRead(titulo)
            dismiss()

        } catch (e: Exception) {
            Timber.d(e, "[Timber] (BottomSheetFeedFragment) updateMarkFeedAsReadFromBottomSheetFeedMenu() ERROR: %s", e.message)
        }
    }

    fun removeFeedFromFavorites(titulo: String) {
        try {
            Timber.d("[Timber] (BottomSheetFeedFragment) removeFeedFromFavorites(%s)", titulo)
            mainSharedViewModel.updateFeedFavoriteState(titulo, false)
            dismiss()

        } catch (e: Exception) {
            Timber.d(e, "[Timber] (BottomSheetFeedFragment) removeFeedFromFavorites() ERROR: %s", e.message)
        }
    }

    fun addFeedToFavorites(titulo: String) {
        try {
            Timber.d("[Timber] (BottomSheetFeedFragment) addFeedToFavorites(%s)", titulo)
            mainSharedViewModel.updateFeedFavoriteState(titulo, true)
            dismiss()

        } catch (e: Exception) {
            Timber.d(e, "[Timber] (BottomSheetFeedFragment) addFeedToFavorites() ERROR: %s", e.message)
        }
    }

    fun unfollowFeed(titulo: String) {
        try {
            Timber.d("[Timber] (BottomSheetFeedFragment) unfollowFeed(%s)", titulo)
            mainSharedViewModel.deleteFeed(titulo)
            dismiss()

        } catch (e: Exception) {
            Timber.d(e, "[Timber] (BottomSheetFeedFragment) unfollowFeed() ERROR: %s", e.message)
        }
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