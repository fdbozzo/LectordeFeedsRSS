package com.blogspot.fdbozzo.lectorfeedsrss.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blogspot.fdbozzo.lectorfeedsrss.R

class AddContentsFragment : Fragment() {

    companion object {
        fun newInstance() = AddContentsFragment()
    }

    private lateinit var viewModel: AddContentsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_contents_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddContentsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}