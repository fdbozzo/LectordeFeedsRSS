package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.blogspot.fdbozzo.lectorfeedsrss.MainSharedViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.R
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.FeedChannelFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.ui.SealedClassAppScreens
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class FeedChannelFragment : Fragment() {

    companion object {
        fun newInstance() = FeedChannelFragment()
    }

    private var _binding: FeedChannelFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mainSharedViewModel: MainSharedViewModel
    private lateinit var navController: NavController
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("[Timber] onCreateView")
        _binding = FeedChannelFragmentBinding.inflate(inflater, container, false)

        val localDatabase = FeedDatabase.getInstance(requireContext())
        val feedRepository = FeedRepository(RoomDataSource(localDatabase), RssFeedDataSource())
        val sharedViewModel: MainSharedViewModel by activityViewModels { MainSharedViewModel.Factory(feedRepository) }
        mainSharedViewModel = sharedViewModel
        mainSharedViewModel.setActiveScreen(SealedClassAppScreens.FeedChannelFragment)

        Timber.i("[Timber] onCreateView() - mainSharedViewModel.fragmento: %s", mainSharedViewModel.testigo)
        mainSharedViewModel.testigo = FeedChannelFragment::class.java.canonicalName

        binding.lifecycleOwner = this // Para que LiveData sea consciente del LifeCycle y se actualice la uI
        mAuth = Firebase.auth

        navController = findNavController()

        /** Adapter para el RecyclerView **/
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())


        /** Observer para los items actualizados y su reflejo en el recycler **/
        mainSharedViewModel.items.observe(viewLifecycleOwner, Observer {
            it?.let {
                //adapter.data = it   // Esto solo lo usa el RecyclerView.Adapter
                //adapter.submitList(it)
                //initRecyclerView(it)
                Timber.i("[Timber] onViewCreated() - mainSharedViewModel.items (CAMBIO)")
                binding.recyclerView.adapter = FeedChannelAdapter(it, sharedViewModel, requireContext())
            }
        })

        /** Observer para el navigateToFeedContentsItem **/
        /*
        sharedViewModel.selectedFeedChannelItemWithFeed.observe(viewLifecycleOwner, Observer { feedChannelItemWithFeed ->
            feedChannelItemWithFeed?.let {
                val action =
                    FeedChannelFragmentDirections.actionFeedContentsFragmentToContentsFragment(
                        feedChannelItemWithFeed.link, feedChannelItemWithFeed.id
                    )
                //NavHostFragment.findNavController(this).navigate(action)
                findNavController().navigate(action)
                sharedViewModel.navigateToContentsWithUrlIsDone()
            }
        })
         */
        mainSharedViewModel.navigateToContents.observe(viewLifecycleOwner, Observer {
            if (it == true && sharedViewModel.lastSelectedFeedChannelItemWithFeed != null) {
                val action =
                    sharedViewModel.lastSelectedFeedChannelItemWithFeed?.let { it1 ->
                        FeedChannelFragmentDirections.actionFeedContentsFragmentToContentsFragment(
                            sharedViewModel.lastSelectedFeedChannelItemWithFeed!!.link,
                            it1.id
                        )
                    }
                //NavHostFragment.findNavController(this).navigate(action)
                if (action != null) {
                    findNavController().navigate(action)
                }
                sharedViewModel.navigateToContentsWithUrlIsDone()
            }
        })



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("[Timber] onViewCreated() - mainSharedViewModel.fragmento: %s", mainSharedViewModel.testigo)
        mainSharedViewModel.testigo = FeedChannelFragment::class.java.canonicalName

        /**
         * Si el usuario no está validado, enviarlo al login
         */
        if (mAuth.currentUser == null) {
            //navGraph.startDestination = R.id.nav_feed_contents
            //navController.popBackStack(R.id.navigation_login, true)
            navController.navigate(R.id.nav_login)
        }


        /** Observer para el Snackbar **/
        mainSharedViewModel.snackBarMessage.observe(viewLifecycleOwner, Observer { messageRId ->
            if (messageRId != null) { // Observed state is true.
                val v = activity?.findViewById<CoordinatorLayout>(R.id.mainCoordinatorLayout)
                Timber.d("[Timber] (FeedChannelFragment) sharedViewModel.snackBarMessage.observe: %s", getString(messageRId))
                if (v != null) {
                    Snackbar.make(
                        v,
                        getString(messageRId),
                        Snackbar.LENGTH_LONG // How long to display the message.
                    ).show()
                }
                mainSharedViewModel.snackBarMessageDone()
            }
        })

    }

}