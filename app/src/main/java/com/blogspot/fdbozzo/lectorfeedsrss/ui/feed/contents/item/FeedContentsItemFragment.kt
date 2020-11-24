package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed.contents.item

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blogspot.fdbozzo.lectorfeedsrss.R
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.FeedContentsItemFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.SettingsFragmentBinding

class FeedContentsItemFragment : Fragment() {

    companion object {
        fun newInstance() = FeedContentsItemFragment()
    }

    private lateinit var viewModelFeed: FeedContentsItemViewModel
    private var _binding: FeedContentsItemFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FeedContentsItemFragmentBinding.inflate(inflater, container, false)

        return binding.root
        //return inflater.inflate(R.layout.feed_contents_item_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModelFeed = ViewModelProvider(this).get(FeedContentsItemViewModel::class.java)
        // TODO: Use the ViewModel
    }

}