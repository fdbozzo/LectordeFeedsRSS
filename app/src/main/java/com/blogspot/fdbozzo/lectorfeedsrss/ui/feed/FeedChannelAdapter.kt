package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.fdbozzo.lectorfeedsrss.MainSharedViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItem as DomainFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItemWithFeed as DomainFeedChannelItemWithFeed
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.FeedChannelItemFragmentBinding
import timber.log.Timber

class FeedChannelAdapter(
    private val list: List<DomainFeedChannelItemWithFeed>,
    private val sharedViewViewModel: MainSharedViewModel,
    val context: Context) : RecyclerView.Adapter<FeedChannelAdapter.ViewHolder>() {
//class FeedChannelAdapter: ListAdapter<FeedChannelItem, FeedChannelAdapter.ViewHolder>(FeedContentsDiff()) {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        Timber.d("onAttachedToRecyclerView")
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Timber.d("onCreateViewHolder")
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //val item = getItem(position)
        val item = list[position]
        holder.bind(item, sharedViewViewModel)
    }

    override fun getItemCount(): Int = list.size

    //class ViewHolder(val fechaFeed: TextView) : RecyclerView.ViewHolder(fechaFeed)
    /**
     * ViewHolder para los datos
     */

    class ViewHolder private constructor(
        val binding: FeedChannelItemFragmentBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(feedChannelItemWithFeed: DomainFeedChannelItemWithFeed, viewModel: MainSharedViewModel) {
            binding.feedChannelItemWithFeed = feedChannelItemWithFeed
            binding.viewModel = viewModel
            //binding.clickListener = clickListener
            Timber.d("[Timber] ViewHolder.bind(%s)", feedChannelItemWithFeed.link)
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FeedChannelItemFragmentBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

/**
 * News item diff
 */
class FeedContentsDiff : DiffUtil.ItemCallback<DomainFeedChannelItem>() {

    override fun areItemsTheSame(oldChannelItem: DomainFeedChannelItem, newChannelItem: DomainFeedChannelItem): Boolean {
        //return oldChannelItem.pubDate == newChannelItem.pubDate
        return oldChannelItem.id == newChannelItem.id
    }

    override fun areContentsTheSame(oldChannelItem: DomainFeedChannelItem, newChannelItem: DomainFeedChannelItem): Boolean {
        return oldChannelItem == newChannelItem
    }
}