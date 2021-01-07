package com.blogspot.fdbozzo.lectorfeedsrss.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.blogspot.fdbozzo.lectorfeedsrss.MainSharedViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.BottomSheetFeedOptionsMenuFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.ui.SealedClassAppScreens
import com.blogspot.fdbozzo.lectorfeedsrss.ui.feed.FeedChannelFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class BottomSheetFeedOptionsMenuFragment(val tituloMenu: String): BottomSheetDialogFragment() {

    private var _binding: BottomSheetFeedOptionsMenuFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mainSharedViewModel: MainSharedViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetFeedOptionsMenuFragmentBinding.inflate(inflater, container, false)
        Timber.d("[Timber] onCreateView() -- BOTTOM SHEET (%s)", tituloMenu)

        val localDatabase = FeedDatabase.getInstance(requireContext())
        val feedRepository = FeedRepository(RoomDataSource(localDatabase), RssFeedDataSource())
        val sharedViewModel: MainSharedViewModel by activityViewModels { MainSharedViewModel.Factory(feedRepository) }
        mainSharedViewModel = sharedViewModel
        //mainSharedViewModel.setActiveScreen(SealedClassAppScreens.FeedChannelFragment)
        binding.lifecycleOwner = this // Para que LiveData sea consciente del LifeCycle y se actualice la uI

        mainSharedViewModel.tituloMenuFeed = tituloMenu
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

    fun removeFeedFromFavorites(nombre: String) {
        Timber.d("[Timber] (BottomSheetFragment) removeFeedFromFavorites(%s)", nombre)
        //mainSharedViewModel.renameGroup(nombre)
        // TODO: SE DEBE LLAMAR AL MISMO METODO DEL VIEWMODEL, PARA QUE LE PONGA EL FLAG favorite=0 AL FEED
        dismiss()
    }

    fun addFeedToFavorites(nombre: String) {
        Timber.d("[Timber] (BottomSheetFragment) addFeedToFavorites(%s)", nombre)
        //mainSharedViewModel.renameGroup(nombre)
        // TODO: SE DEBE LLAMAR AL MISMO METODO DEL VIEWMODEL, PARA QUE LE PONGA EL FLAG favorite=1 AL FEED
        dismiss()
    }

    fun unfollowFeed(titulo: String) {
        Timber.d("[Timber] (BottomSheetFragment) unfollowFeed(%s)", titulo)
        //mainSharedViewModel.updateMarkGroupAsReadFromBottomSheetFeedMenu(titulo)
        // TODO: SE DEBE LLAMAR A LA PANTALLA DE CONFIRMACIÓN (SI/NO) DE UNFOLLOW FEED (QUE LO BORRA DE BBDD)
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