package com.blogspot.fdbozzo.lectorfeedsrss

import android.content.Context
import androidx.lifecycle.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group as DomainGroup
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannel as DomainFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed
import com.blogspot.fdbozzo.lectorfeedsrss.ui.feed.RssApiStatus
import kotlinx.coroutines.launch
import timber.log.Timber
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItemWithFeed as DomainFeedChannelItemWithFeed

class MainSharedViewModel(private val feedRepository: FeedRepository) : ViewModel() {

    var testigo: String? = ""

    private lateinit var rssApiResponse: RssResponse<Feed>

    private var apiBaseUrl = "https://hardzone.es" // "http://blog.mozilla.com/" // "https://hardzone.es/"

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
    fun navigateToContentsWithUrlIsDone() {
        _contentsUrl.value = null
    }

    val items: LiveData<List<DomainFeedChannelItemWithFeed>> = feedRepository.getFeeds().asLiveData()

    /**
     * Llamar a getRssFeedData() en el init para obtener los datos inmediatamente.
     */
    init {

        // Cargamos datos iniciales en el drawer
        viewModelScope.launch { setupInitialDrawerMenuData() }


        viewModelScope.launch {
            //setupInitialDrawerMenuData()

            rssApiResponse = feedRepository.checkNetworkFeeds(apiBaseUrl)

            when (rssApiResponse) {
                is RssResponse.Success -> {
                    // TODO: Falta filtrar los items leidos antes de actualizar el LiveData
                    Timber.d("RssResponse.Success!")
                }
                is RssResponse.Error -> {
                    // TODO: Falta controlar errores
                    Timber.d("RssResponse.Error = ${(rssApiResponse as RssResponse.Error).exception.message}")
                }
            }
        }
    }

    override fun onCleared() {
        Timber.d("onCleared() - mainSharedViewModel.fragmento: %s", testigo)
        super.onCleared()
    }

    private suspend fun setupInitialDrawerMenuData() {

        // Compruebo si existe el último grupo, y si no existe borro toto y relleno
        val groupId = feedRepository.getGroupIdByName("Hardware")
        Timber.d("setupInitialDrawerMenuData() - group.id = %d", groupId)

        if (groupId != 0L) {
            return
        }

        // Borro la bbdd
        feedRepository.deleteAllLocalGroups()

        val gId1 = feedRepository.saveLocalGroup(DomainGroup()) // "Uncategorized"
        val fId1 = feedRepository.saveLocalFeed(
            DomainFeed(
                groupId = gId1,
                linkName = "Stéphane Graber's website",
                link = "https://stgraber.org"
            )
        )
        val cId1 = feedRepository.saveLocalFeedChannel(
            DomainFeedChannel(
                feedId = fId1,
                title = "Stéphane Graber's website",
                description = "Stéphane Graber's Feed",
                link = "https://stgraber.org/feed"
            )
        )

        val gId2 = feedRepository.saveLocalGroup(DomainGroup(groupName = "Tecnología y Ciencia"))
        val fId2 = feedRepository.saveLocalFeed(
                DomainFeed(
                    groupId = gId2,
                    linkName = "HardZone",
                    link = "https://hardzone.es"
                )
            )
        val cId2 = feedRepository.saveLocalFeedChannel(
            DomainFeedChannel(
                feedId = fId2,
                title = "HardZone",
                description = "HardZone Feed",
                link = "https://hardzone.es"
            )
        )

        val gId3 = feedRepository.saveLocalGroup(DomainGroup(groupName = "Hogar"))
        val fId3 = feedRepository.saveLocalFeed(
            DomainFeed(
                groupId = gId3,
                linkName = "EcoInventos",
                link = "https://ecoinventos.com"
            )
        )
        val cId3 = feedRepository.saveLocalFeedChannel(
            DomainFeedChannel(
                feedId = fId3,
                title = "EcoInventos",
                description = "EcoInventos Feed",
                link = "https://ecoinventos.com"
            )
        )

        val gId4 = feedRepository.saveLocalGroup(DomainGroup(groupName = "Android"))
        val fId4 = feedRepository.saveLocalFeed(
            DomainFeed(
                groupId = gId4,
                linkName = "Android Police",
                link = "https://www.androidpolice.com"
            )
        )
        val cId4 = feedRepository.saveLocalFeedChannel(
            DomainFeedChannel(
                feedId = fId4,
                title = "Android Police",
                description = "Android Police Feed",
                link = "https://www.androidpolice.com"
            )
        )

        val gId5 = feedRepository.saveLocalGroup(DomainGroup(groupName = "Hardware"))
        val fId5 = feedRepository.saveLocalFeed(
            DomainFeed(
                groupId = gId5,
                linkName = "El Chapuzas Informático",
                link = "https://elchapuzasinformatico.com"
            )
        )
        val cId5 = feedRepository.saveLocalFeedChannel(
            DomainFeedChannel(
                feedId = fId5,
                title = "El Chapuzas Informático",
                description = "El Chapuzas Informático Feed",
                link = "https://elchapuzasinformatico.com"
            )
        )
    }

    class Factory(private val context: Context, private val feedRepository: FeedRepository) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            Timber.i("Factory() - mainSharedViewModel.fragmento: %s", MainSharedViewModel::testigo)
            return MainSharedViewModel(feedRepository) as T
            //return MainSharedViewModel.getInstance(context, feedRepository) as T
        }
    }

    /*
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
     */
}