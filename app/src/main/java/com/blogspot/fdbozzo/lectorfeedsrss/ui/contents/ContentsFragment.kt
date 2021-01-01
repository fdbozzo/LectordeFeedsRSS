package com.blogspot.fdbozzo.lectorfeedsrss.ui.contents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.ContentsFragmentBinding

class ContentsFragment : Fragment() {

    companion object {
        fun newInstance() = ContentsFragment()
    }

    private lateinit var viewModel: ContentsViewModel
    private var _binding: ContentsFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ContentsFragmentBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this // Para que LiveData sea consciente del LifeCycle y se actualice la uI

        val arguments = ContentsFragmentArgs.fromBundle(requireArguments())
        binding.webView.loadUrl(arguments.feedChannelItemUrl)


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ContentsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}