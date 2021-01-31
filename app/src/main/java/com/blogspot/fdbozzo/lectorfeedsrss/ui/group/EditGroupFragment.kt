package com.blogspot.fdbozzo.lectorfeedsrss.ui.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.blogspot.fdbozzo.lectorfeedsrss.MainSharedViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.R
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.EditGroupFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.ui.SealedClassAppScreens
import com.blogspot.fdbozzo.lectorfeedsrss.ui.contents.ContentsFragmentArgs
import com.blogspot.fdbozzo.lectorfeedsrss.util.hideKeyboard
import kotlinx.coroutines.launch
import timber.log.Timber


class EditGroupFragment() : Fragment() {

    companion object {
        fun newInstance() = EditGroupFragment()
    }

    //private lateinit var viewModel: AddGroupViewModel
    private var _binding: EditGroupFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mainSharedViewModel: MainSharedViewModel
    private lateinit var navController: NavController
    private var originalGroupName = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("[Timber] EditGroupFragment.onCreateView")
        _binding = EditGroupFragmentBinding.inflate(inflater, container, false)

        val localDatabase = FeedDatabase.getInstance(requireContext())
        val feedRepository = FeedRepository(RoomDataSource(localDatabase), RssFeedDataSource())
        val sharedViewModel: MainSharedViewModel by activityViewModels { MainSharedViewModel.Factory(feedRepository) }
        mainSharedViewModel = sharedViewModel
        mainSharedViewModel.setActiveScreen(SealedClassAppScreens.EditGroupFragment)
        //viewModel = ViewModelProvider(this).get(AddGroupViewModel::class.java)

        Timber.i("[Timber] onCreateView() - mainSharedViewModel.fragmento: %s", mainSharedViewModel.testigo)
        mainSharedViewModel.testigo = EditGroupFragment::class.java.canonicalName

        binding.lifecycleOwner = this // Para que LiveData sea consciente del LifeCycle y se actualice la uI
        binding.fragment = this
        navController = findNavController()
        val arguments = EditGroupFragmentArgs.fromBundle(requireArguments())
        originalGroupName = arguments.originalGroupName
        binding.editGroupName.setText(originalGroupName)
        //binding.editGroupName.requestFocus()

        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)

        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)

        return binding.root
        //return inflater.inflate(R.layout.add_group_fragment, container, false)
    }

    fun clickSave() {
        hideKeyboard()
        val groupName = binding.editGroupName.text.toString()
        Timber.d("[Timber] Cick! %s", groupName)

        lifecycleScope.launch {
            saveGroup(groupName)
        }
    }

    suspend fun saveGroup(groupName: String) {
        if (groupName.isNotBlank()) {
            val group = mainSharedViewModel.getGroupByName(originalGroupName)
            if (group != null) {
                group.groupName = groupName
                mainSharedViewModel.updateGroup(group)
                navController.popBackStack()

                val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)

                if (!drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.openDrawer(GravityCompat.START)
            }
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
        //binding.editGroupName.requestFocus()
    }
}