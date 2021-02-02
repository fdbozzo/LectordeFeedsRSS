package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.blogspot.fdbozzo.lectorfeedsrss.MainSharedViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.R
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.ChannelFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.ui.SealedClassAppScreens
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import timber.log.Timber

class ChannelFragment : Fragment() {

    companion object {
        fun newInstance() = ChannelFragment()
    }

    private var _binding: ChannelFragmentBinding? = null
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
        Timber.d("[Timber] ChannelFragment.onCreateView")
        _binding = ChannelFragmentBinding.inflate(inflater, container, false)

        val localDatabase = FeedDatabase.getInstance(requireContext())
        val feedRepository = FeedRepository(RoomDataSource(localDatabase), RssFeedDataSource())
        val sharedViewModel: MainSharedViewModel by activityViewModels { MainSharedViewModel.Factory(feedRepository) }
        mainSharedViewModel = sharedViewModel
        mainSharedViewModel.setActiveScreen(SealedClassAppScreens.ChannelFragment)

        Timber.i("[Timber] onCreateView() - mainSharedViewModel.fragmento: %s", mainSharedViewModel.testigo)
        mainSharedViewModel.testigo = ChannelFragment::class.java.canonicalName

        binding.lifecycleOwner = this // Para que LiveData sea consciente del LifeCycle y se actualice la uI
        mAuth = Firebase.auth

        navController = findNavController()

        Timber.d("[Timber] mAuth.currentUser = %s", mAuth.currentUser)

        if (mAuth.currentUser != null) {
            // USUARIO AUTENTICADO
            /** Adapter para el RecyclerView **/
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())


            /** Observer para los items actualizados y su reflejo en el recycler **/
            mainSharedViewModel.items.observe(viewLifecycleOwner, Observer { listItemWithFeed ->
                listItemWithFeed?.let {
                    try {
                        //adapter.data = it   // Esto solo lo usa el RecyclerView.Adapter
                        //adapter.submitList(it)
                        //initRecyclerView(it)
                        Timber.i("[Timber] onViewCreated() - mainSharedViewModel.items.observe (CAMBIO)")
                        binding.recyclerView.adapter =
                            ChannelAdapter(listItemWithFeed, sharedViewModel, requireContext())

                    } catch (e: Exception) {
                        Timber.d(
                            e,
                            "[Timber] onViewCreated() - mainSharedViewModel.items.observe ERROR: %s",
                            e.message
                        )
                    }
                }
            })

            /**
             * Listener para el swipeRefresh, que permite detectar el arrastre hacia abajo
             * cuando se está al inicio de la lista de items, mostrando una animación de carga.
             */
            binding.swipeRefresh.setOnRefreshListener { swipeRefreshListener() }

            mainSharedViewModel.navigateToContents.observe(viewLifecycleOwner, Observer {
                if (it == true && sharedViewModel.lastSelectedItemWithFeed != null) {
                    val action =
                        sharedViewModel.lastSelectedItemWithFeed?.let { it1 ->
                            ChannelFragmentDirections.actionFeedContentsFragmentToContentsFragment(
                                sharedViewModel.lastSelectedItemWithFeed!!.link,
                                it1.id
                            )
                        }
                    //NavHostFragment.findNavController(this).navigate(action)
                    if (action != null) {
                        navController.navigate(action)
                    }
                    sharedViewModel.navigateToContentsWithUrlIsDone()
                }
            })

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("[Timber] onViewCreated() - mainSharedViewModel.fragmento: %s", mainSharedViewModel.testigo)
        mainSharedViewModel.testigo = ChannelFragment::class.java.canonicalName

        /**
         * Si el usuario no está validado, enviarlo al login
         */
        if (mAuth.currentUser == null) {
            // USUARIO NO AUTENTICADO
            //navGraph.startDestination = R.id.nav_feed_contents
            //navController.popBackStack(R.id.navigation_login, true)
            navController.navigate(R.id.nav_login)
        }


    }

    private fun swipeRefreshListener() {

        lifecycleScope.launch {
            mainSharedViewModel.refreshActiveFeeds()

            if(binding.swipeRefresh.isRefreshing){
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

}