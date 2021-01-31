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
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.BottomSheetMarkAsReadOptionsMenuFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber

class BottomSheetMarkAsReadOptionsMenuFragment(val tituloMenu: String): BottomSheetDialogFragment() {

    private var _binding: BottomSheetMarkAsReadOptionsMenuFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mainSharedViewModel: MainSharedViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetMarkAsReadOptionsMenuFragmentBinding.inflate(inflater, container, false)
        Timber.d("[Timber] onCreateView() -- BOTTOM SHEET (%s)", tituloMenu)

        val localDatabase = FeedDatabase.getInstance(requireContext())
        val feedRepository = FeedRepository(RoomDataSource(localDatabase), RssFeedDataSource())
        val sharedViewModel: MainSharedViewModel by activityViewModels { MainSharedViewModel.Factory(feedRepository) }
        mainSharedViewModel = sharedViewModel
        //mainSharedViewModel.setActiveScreen(SealedClassAppScreens.ChannelFragment)
        binding.lifecycleOwner = this // Para que LiveData sea consciente del LifeCycle y se actualice la uI

        mainSharedViewModel.tituloMenuGroup = tituloMenu
        binding.viewModel = mainSharedViewModel
        binding.fragment = this

        return binding.root
        //return inflater.inflate(R.layout.bottom_sheet_group_options_menu_fragment, container, false)
    }

    fun updateMarkAllArticlesAsReadFromBottomSheetFeedMenu() {
        try {
            Timber.d("[Timber] (BottomSheetFragment) updateMarkAllArticlesAsReadFromBottomSheetFeedMenu()")
            mainSharedViewModel.updateMarkAllFeedAsRead()
            dismiss()

        } catch (e: Exception) {
            Timber.d(e, "[Timber] (BottomSheetGroupFragment) updateMarkFeedAsReadFromBottomSheetFeedMenu() ERROR: %s", e.message)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.txtTituloMenu.text = tituloMenu
    }
}