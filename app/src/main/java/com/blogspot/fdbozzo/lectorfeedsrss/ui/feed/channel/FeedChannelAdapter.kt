package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed.channel

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.item.FeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.FeedChannelItemFragmentBinding
import timber.log.Timber

class FeedChannelAdapter(private val list: List<FeedChannelItem>, val context: Context) : RecyclerView.Adapter<FeedChannelAdapter.ViewHolder>() {
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
        holder.bind(item)
    }

    override fun getItemCount(): Int = list.size

    //class ViewHolder(val fechaFeed: TextView) : RecyclerView.ViewHolder(fechaFeed)
    /**
     * ViewHolder para los datos
     */

    class ViewHolder private constructor(val binding: FeedChannelItemFragmentBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(channelItem: FeedChannelItem) {
            binding.channelItem = channelItem
            //binding.clickListener = clickListener
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
class FeedContentsDiff : DiffUtil.ItemCallback<FeedChannelItem>() {

    override fun areItemsTheSame(oldChannelItem: FeedChannelItem, newChannelItem: FeedChannelItem): Boolean {
        return oldChannelItem.pubDate == newChannelItem.pubDate
    }

    override fun areContentsTheSame(oldChannelItem: FeedChannelItem, newChannelItem: FeedChannelItem): Boolean {
        return oldChannelItem == newChannelItem
    }
}