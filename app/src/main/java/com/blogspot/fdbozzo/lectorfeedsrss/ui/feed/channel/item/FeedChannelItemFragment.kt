package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed.channel.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blogspot.fdbozzo.lectorfeedsrss.R
// TODO: VOLVER A PONER LO QUE ESTÁ COMENTADO
//import com.blogspot.fdbozzo.lectorfeedsrss.databinding.FeedChannelItemFragmentBinding

class FeedChannelItemFragment : Fragment() {

    companion object {
        fun newInstance() = FeedChannelItemFragment()
    }

    private lateinit var viewModelFeed: FeedChannelItemViewModel
//    private var _binding: FeedChannelItemFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
//    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        _binding =  FeedChannelItemFragmentBinding.inflate(inflater, container, false)

//        return binding.root
        return inflater.inflate(R.layout.feed_channel_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModelFeed = ViewModelProvider(this).get(FeedChannelItemViewModel::class.java)
        // TODO: Use the ViewModel
    }

}