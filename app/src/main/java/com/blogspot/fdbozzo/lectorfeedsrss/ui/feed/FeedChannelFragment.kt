package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.blogspot.fdbozzo.lectorfeedsrss.MainViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.R
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItem as DomainFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.FeedChannelFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
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
    private lateinit var viewModel: FeedChannelViewModel
    private lateinit var navController: NavController
    private lateinit var navGraph: NavGraph
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mainViewModel: MainViewModel
    private lateinit var localDatabase: FeedDatabase

    // Valores de ejemplo para RecyclerView
    private val items = listOf(
        "Mon 6/23 - Sunny - 31/17",
        "Tue 6/24 - Foggy - 21/8",
        "Wed 6/25 - Cloudy - 22/17",
        "Thurs 6/26 - Rainy - 18/11",
        "Fri 6/27 - Foggy - 21/10",
        "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
        "Sun 6/29 - Sunny - 20/7"
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView")
        _binding = FeedChannelFragmentBinding.inflate(inflater, container, false)

        localDatabase = FeedDatabase.getInstance(requireContext())
        val contentDS = localDatabase.getFeedChannelItemDao()
        val feedRepository = FeedRepository(RoomDataSource(localDatabase), RssFeedDataSource())
        //val viewModelFactory = FeedChannelViewModelFactory(contentDS)
        val viewModelFactory = FeedChannelViewModelFactory(feedRepository)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        viewModel = ViewModelProvider(this, viewModelFactory).get(FeedChannelViewModel::class.java)

        binding.lifecycleOwner = this // Para que LiveData sea consciente del LifeCycle y se actualice la uI
        mAuth = Firebase.auth

        navController = findNavController()

        /** Setup de retrofit para leer RSS **/
        //val service = RetrofitFactory.makeRetrofitService()

        /** Adapter para el RecyclerView **/
        //val adapter = FeedChannelAdapter()
        //binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())


        //*
        viewModel.items.observe(viewLifecycleOwner, Observer {
            it?.let {
                //adapter.data = it   // Esto solo lo usa el RecyclerView.Adapter
                //adapter.submitList(it)
                //initRecyclerView(it)
                binding.recyclerView.adapter = FeedChannelAdapter(it, viewModel, requireContext())
            }
        })
         //*/

        /** Observer para el navigateToFeedContentsItem **/
        viewModel.contentsUrl.observe(viewLifecycleOwner, Observer { url ->
            url?.let {
                //val action = SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(night.nightId)
                val action =
                    FeedChannelFragmentDirections.actionFeedContentsFragmentToContentsFragment(
                        url
                    )
                //NavHostFragment.findNavController(this).navigate(action)
                findNavController().navigate(action)
                viewModel.navigateToContentsWithUrl_Done()
            }
        })

        /** Observer para el Snackbar **/
        /*
        viewModel.showSnackBarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    getString(R.string.cleared_message),
                    Snackbar.LENGTH_LONG // How long to display the message.
                ).show()
                sleepTrackerViewModel.doneShowingSnackbar()
            }
        })

         */



        return binding.root
        //return inflater.inflate(R.layout.feed_channel_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.fragmento = FeedChannelFragment::class.java.canonicalName

        /**
         * Si el usuario no está validado, enviarlo al login
         */
        if (mAuth.currentUser == null) {
            //navGraph.startDestination = R.id.nav_feed_contents
            //navController.popBackStack(R.id.navigation_login, true)
            navController.navigate(R.id.nav_login)
        }

        /*
        val feedContents: RecyclerView = binding.feedContents
        feedContents.layoutManager = LinearLayoutManager(requireContext())
        feedContents.adapter = FeedContentsAdapter(channelItems)
         */

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModelFeed = ViewModelProvider(this).get(FeedChannelViewModel::class.java)
        // TODO: Use the ViewModel
    }

    //*
    private fun initRecyclerView(list: List<DomainFeedChannelItem>) {
        //val recyclerview: RecyclerView = findViewById(R.id.recycler_view)
        val recyclerview2 = binding.recyclerView
        recyclerview2.adapter = FeedChannelAdapter(list, viewModel, requireContext())
    }

     //*/


}