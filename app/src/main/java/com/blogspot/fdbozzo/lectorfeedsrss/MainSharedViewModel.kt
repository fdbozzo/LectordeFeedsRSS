package com.blogspot.fdbozzo.lectorfeedsrss

import android.content.Context
import androidx.lifecycle.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed
import com.blogspot.fdbozzo.lectorfeedsrss.ui.feed.RssApiStatus
import kotlinx.coroutines.launch
import timber.log.Timber
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItemWithFeed as DomainFeedChannelItemWithFeed

enum class RssApiStatus { LOADING, ERROR, DONE }

class MainSharedViewModel(private val feedRepository: FeedRepository) : ViewModel() {

    var fragmento: String? = ""

    private lateinit var rssApiResponse: RssResponse<Feed>

    private var apiBaseUrl = "https://hardzone.es/" // "http://blog.mozilla.com/" // "https://hardzone.es/"

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

    val items: LiveData<List<DomainFeedChannelItemWithFeed>> = feedRepository.getFeeds().asLiveData()

    /**
     * Llamar a getRssFeedData() en el init para obtener los datos inmediatamente.
     */
    init {
        viewModelScope.launch {

            rssApiResponse = feedRepository.checkNetworkFeeds(apiBaseUrl)

            when (rssApiResponse) {
                is RssResponse.Success -> {
                    // TODO: Falta filtrar los items leidos antes de actualizar el LiveData
                    Timber.d("RssResponse.Success!}")
                }
                is RssResponse.Error -> {
                    // TODO: Falta controlar errores
                    Timber.d("RssResponse.Error = ${(rssApiResponse as RssResponse.Error).exception.message}")
                }
            }
        }
    }

    override fun onCleared() {
        Timber.i("onCleared() - mainSharedViewModel.fragmento: %s", fragmento)
        super.onCleared()
    }

    class Factory(private val context: Context, private val feedRepository: FeedRepository) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            Timber.i("Factory() - mainSharedViewModel.fragmento: %s", MainSharedViewModel::fragmento)
            return MainSharedViewModel(feedRepository) as T
            //return MainSharedViewModel.getInstance(context, feedRepository) as T
        }
    }

    companion object {

        @Volatile
        private var INSTANCE: MainSharedViewModel? = null

        fun getInstance(context: Context, feedRepository: FeedRepository): MainSharedViewModel {
            synchronized(this) {

                var instance = INSTANCE

                // If instance is `null` make a new instance.
                if (instance == null) {
                    Timber.i("getInstance() - mainSharedViewModel NUEVO")
                    instance = MainSharedViewModel(feedRepository)
                    INSTANCE = instance
                }

                // Return instance; smart cast to be non-null.
                return instance
            }
        }
    }
}