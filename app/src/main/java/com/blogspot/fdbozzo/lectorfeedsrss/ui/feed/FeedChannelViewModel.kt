package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed

import android.content.Context
import androidx.lifecycle.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannel as DomainFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItem as DomainFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedChannelItemDao
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.data.toDomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.toDomainFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.toDomainFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssApi
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import kotlin.coroutines.coroutineContext

enum class RssApiStatus { LOADING, ERROR, DONE }

class FeedChannelViewModel(private val feedRepository: FeedRepository) : ViewModel() {
//class FeedChannelViewModel(private val daoChannel: FeedChannelItemDao) : ViewModel() {

    //private val channelItemDao: FeedChannelItemDao = daoChannel
    private lateinit var rssApiResponse: RssResponse<Feed>

    /*
    private var _items = MutableLiveData<List<DomainFeedChannelItem>>()
    val items: LiveData<List<DomainFeedChannelItem>>
        get() = _items
     */

    private var _channels = MutableLiveData<List<DomainFeedChannel>>()
    val channels: LiveData<List<DomainFeedChannel>>
        get() = _channels

    private var _feeds = MutableLiveData<Response<DomainFeed>>()
    val feeds: LiveData<Response<DomainFeed>>
        get() = _feeds

    private var _status = MutableLiveData<RssApiStatus>()
    val status: LiveData<RssApiStatus>
        get() = _status

    /**
     * FLAGS Y MÉTODOS PARA NAVEGACIÓN A CONTENTS_FRAGMENT (la noticia)
     */
    private var _contentsUrl = MutableLiveData<String>()
    val contentsUrl: LiveData<String>
        get() = _contentsUrl

    fun navigateToContentsWithUrl(url: String) {
        _contentsUrl.value = url
    }
    fun navigateToContentsWithUrl_Done() {
        _contentsUrl.value = null
    }

    val items: LiveData<List<DomainFeedChannelItem>> = feedRepository.getFeeds().asLiveData()

    /**
     * Llamar a getRssFeedData() en el init para obtener los datos inmediatamente.
     */
    init {
        viewModelScope.launch {

            //_items = feedRepository.getFeeds().asLiveData(coroutineContext, 15_000) as MutableLiveData<List<DomainFeedChannelItem>>
            //feedRepository.getFeeds().asLiveData(coroutineContext, 20_000)
            //feedRepository.getFeeds()

            rssApiResponse = feedRepository.checkNetworkFeeds()

            when (rssApiResponse) {
                is RssResponse.Success -> {

                    //val domainFeed = (rssApiResponse as RssResponse.Success<Feed>).data.toDomainFeed()

                    /*
                    val domainFeedChannelItems = (rssApiResponse as RssResponse.Success<Feed>).data.channel.channelItems?.map {
                        it.toDomainFeedChannelItem()
                    }
                     */

                    /** Con la respuesta ahora puedo guardar en BBDD */
                    //feedRepository.saveNetworkFeeds(domainFeed)

                    // TODO: Falta filtrar los items leidos antes de actualizar el LiveData
                    //_items.value = domainFeedChannelItems

                }
                is RssResponse.Error -> {
                    Timber.d("RssResponse.Error = ${(rssApiResponse as RssResponse.Error).exception.message}")
                }
            }
        }
    }

}