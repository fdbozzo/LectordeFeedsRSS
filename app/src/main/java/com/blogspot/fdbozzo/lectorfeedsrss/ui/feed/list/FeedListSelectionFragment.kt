package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed.list

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blogspot.fdbozzo.lectorfeedsrss.R

class FeedListSelectionFragment : Fragment() {

    companion object {
        fun newInstance() = FeedListSelectionFragment()
    }

    private lateinit var viewModel: FeedListSelectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.feed_list_selection_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FeedListSelectionViewModel::class.java)
        // TODO: Use the ViewModel
    }

}