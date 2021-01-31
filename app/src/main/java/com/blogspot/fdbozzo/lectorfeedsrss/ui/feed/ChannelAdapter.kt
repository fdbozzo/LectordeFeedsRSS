package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.fdbozzo.lectorfeedsrss.MainSharedViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Item as DomainItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.ItemWithFeed as DomainItemWithFeed
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.ItemFragmentBinding
import timber.log.Timber

class ChannelAdapter(
    private val list: List<DomainItemWithFeed>,
    private val sharedViewViewModel: MainSharedViewModel,
    val context: Context) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            //val item = getItem(position)
            /*
            Timber.d("onBindViewHolder(position=%d) list[%d]=%s, id=%d, link=%s",
                position, position, list[position], list[position].id, list[position].link)
             */
            val item = list[position]
            holder.bind(item, sharedViewViewModel)

        } catch (e: Exception) {
            Timber.d(e, "[Timber] ChannelFragment.onBindViewHolder() ERROR: %s", e.message)
        }
    }

    override fun getItemCount(): Int = list.size

    /**
     * ViewHolder para los datos
     */

    class ViewHolder private constructor(
        val binding: ItemFragmentBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(itemWithFeed: DomainItemWithFeed, viewModel: MainSharedViewModel) {
            try {
                binding.itemWithFeed = itemWithFeed
                binding.viewModel = viewModel
                //Timber.d("[Timber] ViewHolder.bind(%s)", itemWithFeed.link)
                binding.executePendingBindings()

            } catch (e: Exception) {
                Timber.d(e, "[Timber] ChannelFragment.ViewHolder.bind() ERROR: %s", e.message)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemFragmentBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

/**
 * News item diff
 */
class FeedContentsDiff : DiffUtil.ItemCallback<DomainItem>() {

    override fun areItemsTheSame(oldItem: DomainItem, newItem: DomainItem): Boolean {
        //return oldItem.pubDate == newItem.pubDate
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DomainItem, newItem: DomainItem): Boolean {
        return oldItem == newItem
    }
}