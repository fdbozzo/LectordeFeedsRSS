package com.blogspot.fdbozzo.lectorfeedsrss.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.blogspot.fdbozzo.lectorfeedsrss.MainViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.R
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.FeedContentsFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.LoginFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class FeedContentsFragment : Fragment() {

    companion object {
        fun newInstance() = FeedContentsFragment()
    }

    private lateinit var viewModelFeed: FeedContentsViewModel
    private lateinit var navController: NavController
    private lateinit var navGraph: NavGraph
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FeedContentsFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.feed_contents_fragment, container, false)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        mAuth = Firebase.auth


        navController = findNavController()

        /*
        val navHostFragment =
            parentFragmentManager.findFragmentById(R.id.fragmentContainerView)

        if (navHostFragment != null) {
            navController = navHostFragment.findNavController()
            //val graphInflater = navController.navInflater
            //navGraph = graphInflater.inflate(R.navigation.nav_graph)
        }
         */

        return binding.root
        //return inflater.inflate(R.layout.feed_contents_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("mainViewModel.fragmento: %s", mainViewModel.fragmento)
        mainViewModel.fragmento = FeedContentsFragment::class.java.canonicalName

        /**
         * Si el usuario no está validado, enviarlo al login
         */
        if (mAuth.currentUser == null) {
            //navGraph.startDestination = R.id.nav_feed_contents
            //navController.popBackStack(R.id.navigation_login, true)
            navController.navigate(R.id.nav_login)
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModelFeed = ViewModelProvider(this).get(FeedContentsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}