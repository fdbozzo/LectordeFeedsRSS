package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blogspot.fdbozzo.lectorfeedsrss.R

class AddFeedFragment : Fragment() {

    companion object {
        fun newInstance() = AddFeedFragment()
    }

    private lateinit var viewModel: AddFeedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_feed_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddFeedViewModel::class.java)
        // TODO: Use the ViewModel
    }

}