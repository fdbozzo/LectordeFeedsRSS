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
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group as RoomGroup
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.BottomSheetGroupOptionsMenuFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber

class BottomSheetGroupOptionsMenuFragment(val tituloMenu: String, val group: Group): BottomSheetDialogFragment() {

    private var _binding: BottomSheetGroupOptionsMenuFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mainSharedViewModel: MainSharedViewModel
    private var _deleteGroupVisibility = View.VISIBLE
    val deleteGroupVisibility get() = _deleteGroupVisibility
    private var _renameGroupVisibility = View.VISIBLE
    val renameGroupVisibility get() = _renameGroupVisibility

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetGroupOptionsMenuFragmentBinding.inflate(inflater, container, false)
        Timber.d("[Timber] onCreateView() -- BOTTOM SHEET (%s)", tituloMenu)

        val localDatabase = FeedDatabase.getInstance(requireContext())
        val feedRepository = FeedRepository(RoomDataSource(localDatabase), RssFeedDataSource())
        val sharedViewModel: MainSharedViewModel by activityViewModels { MainSharedViewModel.Factory(feedRepository) }
        mainSharedViewModel = sharedViewModel
        //mainSharedViewModel.setActiveScreen(SealedClassAppScreens.FeedChannelFragment)
        binding.lifecycleOwner = this // Para que LiveData sea consciente del LifeCycle y se actualice la uI


        if (group.groupName == RoomGroup.DEFAULT_NAME) {
            _deleteGroupVisibility = View.GONE
            _renameGroupVisibility = View.GONE
        } else {
            _deleteGroupVisibility = View.VISIBLE
            _renameGroupVisibility = View.VISIBLE
        }

        Timber.d("[Timber] BOTTOM SHEET -> deleteGroupVisibility: %s",
            if (_deleteGroupVisibility == View.VISIBLE) "VISIBLE" else "GONE")
        Timber.d("[Timber] BOTTOM SHEET -> renameGroupVisibility: %s",
            if (_renameGroupVisibility == View.VISIBLE) "VISIBLE" else "GONE")


        mainSharedViewModel.tituloMenuGroup = tituloMenu
        binding.viewModel = mainSharedViewModel
        binding.fragment = this

        return binding.root
        //return inflater.inflate(R.layout.bottom_sheet_group_options_menu_fragment, container, false)
    }

    fun updateMarkGroupAsReadFromBottomSheetFeedMenu(titulo: String) {
        Timber.d("[Timber] (BottomSheetFragment) updateMarkGroupAsReadFromBottomSheetFeedMenu(%s)", titulo)
        mainSharedViewModel.updateMarkGroupAsRead(titulo)
        dismiss()
    }

    fun renameGroup(nombre: String) {
        Timber.d("[Timber] (BottomSheetFragment) renameGroup(%s)", nombre)
        //mainSharedViewModel.renameGroup(nombre)
        // TODO: SE DEBE LLAMAR A LA PANTALLA DE RENAME GROUP
        dismiss()
    }

    fun deleteGroup(titulo: String) {
        Timber.d("[Timber] (BottomSheetFragment) deleteGroup(%s)", titulo)
        mainSharedViewModel.deleteGroup(titulo)
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

        binding.txtTituloMenu.text = tituloMenu

        /*
        listViewOptions.adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            options
        )
         */
    }
}