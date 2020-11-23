package com.blogspot.fdbozzo.lectorfeedsrss.ui.contents

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blogspot.fdbozzo.lectorfeedsrss.R

class ReadLaterFragment : Fragment() {

    companion object {
        fun newInstance() = ReadLaterFragment()
    }

    private lateinit var viewModel: ReadLaterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.read_later_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ReadLaterViewModel::class.java)
        // TODO: Use the ViewModel
    }

}