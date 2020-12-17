package com.blogspot.fdbozzo.lectorfeedsrss.ui.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.AddGroupFragmentBinding

class AddGroupFragment : Fragment() {

    companion object {
        fun newInstance() = AddGroupFragment()
    }

    private lateinit var viewModel: AddGroupViewModel
    private var _binding: AddGroupFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddGroupFragmentBinding.inflate(inflater, container, false)

        return binding.root
        //return inflater.inflate(R.layout.add_group_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddGroupViewModel::class.java)
        // TODO: Use the ViewModel
    }

}