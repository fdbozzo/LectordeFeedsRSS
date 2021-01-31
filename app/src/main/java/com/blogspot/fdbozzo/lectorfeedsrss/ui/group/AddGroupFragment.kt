package com.blogspot.fdbozzo.lectorfeedsrss.ui.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.blogspot.fdbozzo.lectorfeedsrss.MainSharedViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.R
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.AddGroupFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.ui.SealedClassAppScreens
import com.blogspot.fdbozzo.lectorfeedsrss.util.hideKeyboard
import timber.log.Timber

class AddGroupFragment : Fragment() {

    companion object {
        fun newInstance() = AddGroupFragment()
    }

    //private lateinit var viewModel: AddGroupViewModel
    private var _binding: AddGroupFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mainSharedViewModel: MainSharedViewModel
    private lateinit var navController: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("[Timber] AddGroupFragment.onCreateView")
        _binding = AddGroupFragmentBinding.inflate(inflater, container, false)

        val localDatabase = FeedDatabase.getInstance(requireContext())
        val feedRepository = FeedRepository(RoomDataSource(localDatabase), RssFeedDataSource())
        val sharedViewModel: MainSharedViewModel by activityViewModels { MainSharedViewModel.Factory(feedRepository) }
        mainSharedViewModel = sharedViewModel
        mainSharedViewModel.setActiveScreen(SealedClassAppScreens.AddGroupFragment)
        //viewModel = ViewModelProvider(this).get(AddGroupViewModel::class.java)

        Timber.i("[Timber] onCreateView() - mainSharedViewModel.fragmento: %s", mainSharedViewModel.testigo)
        mainSharedViewModel.testigo = AddGroupFragment::class.java.canonicalName

        binding.lifecycleOwner = this // Para que LiveData sea consciente del LifeCycle y se actualice la uI
        //binding.viewModel = viewModel
        binding.fragment = this

        navController = findNavController()
        //binding.editGroupName.requestFocus()

        return binding.root
        //return inflater.inflate(R.layout.add_group_fragment, container, false)
    }

    fun clickAdd() {
        hideKeyboard()
        val groupName = binding.editGroupName.text.toString()
        Timber.d("[Timber] Cick! %s", groupName)

        if (groupName.isNotBlank()) {
            val group = Group(groupName = groupName)
            mainSharedViewModel.addGroup(group)
            binding.editGroupName.text?.clear()
        } else {
            binding.textInputGroupName.error = getString(R.string.err_name_cant_be_empty)
            //binding.editGroupName.requestFocus()
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }
}