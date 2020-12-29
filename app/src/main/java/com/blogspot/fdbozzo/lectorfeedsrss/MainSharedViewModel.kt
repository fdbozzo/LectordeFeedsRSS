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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.ArrayList
import java.util.HashMap
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItemWithFeed as DomainFeedChannelItemWithFeed

class MainSharedViewModel(private val feedRepository: FeedRepository) : ViewModel() {

    var testigo: String? = ""

    private lateinit var rssApiResponse: RssResponse<Feed>

    private var _apiBaseUrl = MutableLiveData<String>()
    val apiBaseUrl: LiveData<String>
        get() = _apiBaseUrl

    // Indicador de si está activo el filtro para los feeds (false=all, true=seleccionado)
    /*
    private var _feedsFilter = MutableLiveData<Boolean>(false)
    val feedsFilter: LiveData<Boolean>
        get() = _feedsFilter
     */

    // Feed seleccionado
    private var _selectedFeed = MutableLiveData(DomainFeed(linkName = "%"))
    val selectedFeed: LiveData<DomainFeed>
        get() = _selectedFeed

    // "https://hardzone.es" // "http://blog.mozilla.com/" // "https://hardzone.es/"

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

    /** Conecta items al origen de BBDD para actualización automática **/
    /* FUNCIONA, PERO NO FILTRA
    val items: LiveData<List<DomainFeedChannelItemWithFeed>> =
        feedRepository.getFilteredFeeds("%").asLiveData()
     */

    /*  NO FUNCIONA (NO FILTRA)
    val items: LiveData<List<DomainFeedChannelItemWithFeed>>? =
        selectedFeed.value?.let { feedRepository.getFilteredFeeds(it.linkName).asLiveData() }
     */

    val items: LiveData<List<DomainFeedChannelItemWithFeed>> = Transformations.switchMap(selectedFeed) { selectedFeed ->
        feedRepository.getFilteredFeeds(selectedFeed.linkName).asLiveData()
    }


    /*
    val items: LiveData<List<DomainFeedChannelItemWithFeed>> =
        feedRepository.getFeeds().asLiveData().map { listFCIWF ->
            listFCIWF.filter {
                Timber.d("[Timber] * it.linkName = '%s', feedsFilter.value = '%s', feedsFilter.value.linkName = '%s'",
                    it.linkName, selectedFeed.value.toString(), selectedFeed.value?.linkName?:"null")
                return@filter it.linkName == "EcoInventos"
                //return@filter feedsFilter.value == true || it.linkName == selectedFeed.value?.linkName ?: "EcoInventos"
                //return@filter selectedFeed.value == null || selectedFeed.value!!.id == 0L || it.linkName == selectedFeed.value?.linkName ?: "HardZone"
            }
        }
     */



    /**
     * Llamar a getRssFeedData() en el init para obtener los datos inmediatamente.
     */
    init {
        Timber.d("[Timber] MainSharedViewModel.init() - apiBaseUrl se cambia a 'https://hardzone.es'")
        _apiBaseUrl.value = "https://hardzone.es"
        //_feedsFilter.value = false

        // Cargamos datos iniciales en el drawer
        viewModelScope.launch { setupInitialDrawerMenuData() }

        // Chequeamos los feeds y sus actualizaciones
        //getFeeds()
    }

    fun getFeeds() {
        Timber.d("[Timber] MainSharedViewModel.getFeeds() - Obtener noticias de  ${_apiBaseUrl.value}")
        if (_apiBaseUrl.value.isNullOrBlank())
            return

        viewModelScope.launch {

            rssApiResponse = feedRepository.checkNetworkFeeds(_apiBaseUrl.value!!)

            when (rssApiResponse) {
                is RssResponse.Success -> {
                    // TODO: Falta filtrar los items leidos antes de actualizar el LiveData
                    Timber.d("[Timber] RssResponse.Success!")
                }
                is RssResponse.Error -> {
                    // TODO: Falta controlar errores
                    Timber.d("[Timber] RssResponse.Error = ${(rssApiResponse as RssResponse.Error).exception.message}")
                }
            }
        }
    }

    override fun onCleared() {
        Timber.d("[Timber] onCleared() - mainSharedViewModel.fragmento: %s", testigo)
        super.onCleared()
    }

    fun getFeedWithLinkNameAndSetApiBaseUrl(linkName: String) {
        viewModelScope.launch {
            val feed = withContext(Dispatchers.IO) {
                feedRepository.getFeedWithLinkName(linkName)
            }
            Timber.d("[Timber] feedRepository.getFeedWithLinkName(%s) = %s", linkName, feed.toString())
            //_selectedFeed.value?.copy(id = feed.id,groupId = feed.groupId,linkName = feed.linkName,link = feed.link,favorite = feed.favorite)
            setSelectedFeed(feed)
            //_feedsFilter.value = true
            _apiBaseUrl.value = feed.link
        }
    }


    fun setSelectedFeed(feed: DomainFeed) {
        Timber.d("[Timber] MainSharedViewModel.setSelectedFeed(DomainFeed.linkName = '%s')", feed.linkName)
        _selectedFeed.postValue(feed)
    }


    private suspend fun setupInitialDrawerMenuData() {

        // Compruebo si existe el último grupo, y si no existe borro toto y relleno
        val groupId = feedRepository.getGroupIdByName("Hardware")
        Timber.d("[Timber] setupInitialDrawerMenuData() - group.id = %d", groupId)

        if (groupId != 0L) {
            /**
             * Si groupId != 0 significa que se encontró el grupo buscado,
             * y que entonces no hay que borrar los datos nuevamente.
             */
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

    /**
     * Lista de valores del menú Drawer
     */
    val menuData: LiveData<HashMap<String, List<String>>> = liveData {
        val listData = HashMap<String, List<String>>()

        if (true) {
            emit(feedRepository.getGroupsWithFeeds())

        } else {

            val redmiMobiles = ArrayList<String>()
            redmiMobiles.add("Redmi Y2")
            redmiMobiles.add("Redmi S2")
            redmiMobiles.add("Redmi Note 5 Pro")
            redmiMobiles.add("Redmi Note 5")
            redmiMobiles.add("Redmi 5 Plus")
            redmiMobiles.add("Redmi Y1")
            redmiMobiles.add("Redmi 3S Plus")

            val micromaxMobiles = ArrayList<String>()
            micromaxMobiles.add("Micromax Bharat Go")
            micromaxMobiles.add("Micromax Bharat 5 Pro")
            micromaxMobiles.add("Micromax Bharat 5")
            micromaxMobiles.add("Micromax Canvas 1")
            micromaxMobiles.add("Micromax Dual 5")

            val appleMobiles = ArrayList<String>()
            appleMobiles.add("iPhone 8")
            appleMobiles.add("iPhone 8 Plus")
            appleMobiles.add("iPhone X")
            appleMobiles.add("iPhone 7 Plus")
            appleMobiles.add("iPhone 7")
            appleMobiles.add("iPhone 6 Plus")

            val samsungMobiles = ArrayList<String>()
            samsungMobiles.add("Samsung Galaxy S9+")
            samsungMobiles.add("Samsung Galaxy Note 7")
            samsungMobiles.add("Samsung Galaxy Note 5 Dual")
            samsungMobiles.add("Samsung Galaxy S8")
            samsungMobiles.add("Samsung Galaxy A8")
            samsungMobiles.add("Samsung Galaxy Note 4")

            listData["Redmi"] = redmiMobiles
            listData["Micromax"] = micromaxMobiles
            listData["Apple"] = appleMobiles
            listData["Samsung"] = samsungMobiles

            emit(listData)
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

    class Factory(private val context: Context, private val feedRepository: FeedRepository) :
        ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            Timber.d("[Timber] Factory() - mainSharedViewModel.fragmento: %s", MainSharedViewModel::testigo)
            return MainSharedViewModel(feedRepository) as T
            //return MainSharedViewModel.getInstance(context, feedRepository) as T
        }
    }

}