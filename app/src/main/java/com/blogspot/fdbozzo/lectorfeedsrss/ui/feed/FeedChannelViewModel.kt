package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed

import androidx.lifecycle.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed
import kotlinx.coroutines.launch
import timber.log.Timber
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItem as DomainFeedChannelItem

enum class RssApiStatus { LOADING, ERROR, DONE }

class FeedChannelViewModel(private val feedRepository: FeedRepository) : ViewModel() {
//class FeedChannelViewModel(private val daoChannel: FeedChannelItemDao) : ViewModel() {

    private lateinit var rssApiResponse: RssResponse<Feed>

    /*
    private var _channels = MutableLiveData<List<DomainFeedChannel>>()
    val channels: LiveData<List<DomainFeedChannel>>
        get() = _channels

    private var _feeds = MutableLiveData<Response<DomainFeed>>()
    val feeds: LiveData<Response<DomainFeed>>
        get() = _feeds
     */

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

            rssApiResponse = feedRepository.checkNetworkFeeds()

            when (rssApiResponse) {
                is RssResponse.Success -> {
                    // TODO: Falta filtrar los items leidos antes de actualizar el LiveData
                }
                is RssResponse.Error -> {
                    // TODO: Falta controlar errores
                    Timber.d("RssResponse.Error = ${(rssApiResponse as RssResponse.Error).exception.message}")
                }
            }
        }
    }

}