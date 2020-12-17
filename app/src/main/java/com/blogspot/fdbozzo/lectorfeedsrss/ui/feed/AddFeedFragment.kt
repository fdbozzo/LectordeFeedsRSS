package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.AddFeedFragmentBinding

class AddFeedFragment : Fragment() {

    companion object {
        fun newInstance() = AddFeedFragment()
    }

    private var _binding: AddFeedFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var viewModel: AddFeedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddFeedFragmentBinding.inflate(inflater, container, false)

        return binding.root
        //return inflater.inflate(R.layout.add_feed_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddFeedViewModel::class.java)
        // TODO: Use the ViewModel
    }

}