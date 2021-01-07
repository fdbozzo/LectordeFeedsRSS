package com.blogspot.fdbozzo.lectorfeedsrss.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import com.blogspot.fdbozzo.lectorfeedsrss.MainSharedViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.R
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.BottomSheetGroupOptionsMenuFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber

class BottomSheetGroupOptionsMenuFragment(val tituloMenu: String): BottomSheetDialogFragment() {

    private var _binding: BottomSheetGroupOptionsMenuFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mainSharedViewModel: MainSharedViewModel


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
        //mainSharedViewModel.updateMarkGroupAsReadFromBottomSheetFeedMenu(titulo)
        // TODO: SE DEBE LLAMAR A LA PANTALLA DE CONFIRMACIÓN (SI/NO) DE DELETE GROUP
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