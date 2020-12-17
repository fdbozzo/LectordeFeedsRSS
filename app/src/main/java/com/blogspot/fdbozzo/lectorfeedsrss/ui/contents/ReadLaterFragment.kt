package com.blogspot.fdbozzo.lectorfeedsrss.ui.contents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.ReadLaterFragmentBinding

class ReadLaterFragment : Fragment() {

    companion object {
        fun newInstance() = ReadLaterFragment()
    }

    private lateinit var viewModel: ReadLaterViewModel
    private var _binding: ReadLaterFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ReadLaterFragmentBinding.inflate(inflater, container, false)

        return binding.root
        //return inflater.inflate(R.layout.read_later_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ReadLaterViewModel::class.java)
        // TODO: Use the ViewModel
    }

}