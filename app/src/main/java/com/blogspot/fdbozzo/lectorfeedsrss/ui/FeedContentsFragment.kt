package com.blogspot.fdbozzo.lectorfeedsrss.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.blogspot.fdbozzo.lectorfeedsrss.R
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.FeedContentsFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.LoginFragmentBinding

class FeedContentsFragment : Fragment() {

    companion object {
        fun newInstance() = FeedContentsFragment()
    }

    private lateinit var viewModelFeed: FeedContentsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FeedContentsFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.feed_contents_fragment, container, false)


        return binding.root
        //return inflater.inflate(R.layout.feed_contents_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModelFeed = ViewModelProvider(this).get(FeedContentsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}