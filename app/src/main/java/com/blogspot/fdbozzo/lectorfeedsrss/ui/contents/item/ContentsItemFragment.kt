package com.blogspot.fdbozzo.lectorfeedsrss.ui.contents.item

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blogspot.fdbozzo.lectorfeedsrss.R

class ContentsItemFragment : Fragment() {

    companion object {
        fun newInstance() = ContentsItemFragment()
    }

    private lateinit var viewModel: ContentsItemViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.feed_contents_item_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ContentsItemViewModel::class.java)
        // TODO: Use the ViewModel
    }

}