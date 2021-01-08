package com.blogspot.fdbozzo.lectorfeedsrss

import androidx.lifecycle.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.SelectedFeedOptions
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssApiStatus
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed
import com.blogspot.fdbozzo.lectorfeedsrss.ui.SealedClassAppScreens
import com.blogspot.fdbozzo.lectorfeedsrss.util.toBoolean
import com.blogspot.fdbozzo.lectorfeedsrss.util.toInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannel as DomainFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItemWithFeed as DomainFeedChannelItemWithFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group as DomainGroup

class MainSharedViewModel(private val feedRepository: FeedRepository) : ViewModel() {

    var testigo: String? = ""

    private lateinit var rssApiResponse: RssResponse<Feed>

    private var _apiBaseUrl = MutableLiveData<String>()
    val apiBaseUrl: LiveData<String>
        get() = _apiBaseUrl

    // Indicador de pantalla activa. Guarda la clase de la pantalla actual
    private var _selectedScreen = MutableLiveData<SealedClassAppScreens>()
    val selectedScreen: LiveData<SealedClassAppScreens>
        get() = _selectedScreen

    // Feed seleccionado
    private var _selectedFeedOptions = MutableLiveData(SelectedFeedOptions())
    val selectedFeedOptions: LiveData<SelectedFeedOptions>
        get() = _selectedFeedOptions

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
    private var _selectedFeedChannelItemWithFeed = MutableLiveData<DomainFeedChannelItemWithFeed?>()
    val selectedFeedChannelItemWithFeed: LiveData<DomainFeedChannelItemWithFeed?>
        get() = _selectedFeedChannelItemWithFeed

    /**
     * Mantiene los datos actualizados del último registro seleccionado.
     * Es actualizado por el observer de autoUpdatedSelectedFeedChannelItemWithFeed.
     */
    private var _lastSelectedFeedChannelItemWithFeed: DomainFeedChannelItemWithFeed? =
        DomainFeedChannelItemWithFeed()
    val lastSelectedFeedChannelItemWithFeed: DomainFeedChannelItemWithFeed?
        get() = _lastSelectedFeedChannelItemWithFeed

    /**
     * Guarda temporalmente el registro seleccionado (con id y link)
     */
    private var _selectedFeedChannelItemId = MutableLiveData<Long>()
    val selectedFeedChannelItemId: LiveData<Long>
        get() = _selectedFeedChannelItemId

    /**
     * Conecta el LiveData lastSelectedFeedChannelItemWithFeed al registro seleccionado
     * con el ID = selectedFeedChannelItemId
     */
    val autoUpdatedSelectedFeedChannelItemWithFeed: LiveData<DomainFeedChannelItemWithFeed> =
        Transformations.switchMap(selectedFeedChannelItemId) { id ->
            feedRepository.getFeedChannelItemWithFeedFlow(id).asLiveData()
        }

    /**
     * Conecta el LiveData "items" a la consulta de BBDD para actualización automática cuando cambien
     * los valores del filtro LiveData "selectedFeedOptions"
     */
    val items: LiveData<List<DomainFeedChannelItemWithFeed>> =
        Transformations.switchMap(selectedFeedOptions) { selectedFeedOptions ->
            feedRepository.getFilteredFeeds(selectedFeedOptions)
                .asLiveData()
        }

    /**
     * Controla la navegación al contenido para leer
     */
    private var _navigateToContents = MutableLiveData<Boolean?>()
    val navigateToContents: LiveData<Boolean?>
        get() = _navigateToContents

    /**
     * ReadLaterStatus contiene el estado de ReadLater: true/false/null
     */
    /*
    private var _readLaterStatus = MutableLiveData<Boolean?>()
    val readLaterStatus: LiveData<Boolean?>
        get() = _readLaterStatus
     */

    /**
     * Guarda el título (nombre de feed) del menú abierto
     */
    var tituloMenuFeed = ""


    /**
     * Guarda el título (nombre de grupo) del menú abierto
     */
    var tituloMenuGroup = ""

    /**
     * LiveData para mensajes SnackBar, donde se indica el R.string.id del mensaje
     */
    private var _snackBarMessage = MutableLiveData<Int?>()
    val snackBarMessage: LiveData<Int?>
        get() = _snackBarMessage


    /**
     * Configurar el LiveData apiBaseUrl en el init para obtener los datos inmediatamente.
     */
    init {
        Timber.d("[Timber] MainSharedViewModel.init() - apiBaseUrl se cambia a 'https://hardzone.es'")
        _apiBaseUrl.value = "https://hardzone.es"

        // Cargamos datos iniciales en el drawer
        viewModelScope.launch { setupInitialDrawerMenuData() }

    }

    fun setActiveScreen(sealedClassAppScreens: SealedClassAppScreens) {
        _selectedScreen.value = sealedClassAppScreens
        Timber.d("[Timber] Pantalla activa: %s", sealedClassAppScreens)
    }


    fun navigateToContentsWithUrlIsDone() {
        Timber.d("[Timber] navigateToContentsWithUrlIsDone()")
        _selectedFeedChannelItemWithFeed.value = null
        //updateItemReadStatus(true)
        _navigateToContents.value = null
    }

    fun snackBarMessageDone() {
        _snackBarMessage.value = null
    }

    /**
     * Actualiza el estado del flag "read" del item elegido
     */
    fun updateItemReadStatus(read: Boolean, id: Long? = selectedFeedChannelItemId.value) {
        if (id != null) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    feedRepository.updateReadStatus(id, read)
                }
            }
        }
    }

    /**
     * Actualiza el estado del flag "read_later" del item elegido
     */
    fun updateItemReadLaterStatus(id: Long? = selectedFeedChannelItemId.value) {
        if (id != null) {
            //val readLater = lastSelectedFeedChannelItemWithFeed.readLater != 1
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        feedRepository.updateInverseReadLaterStatus(id)
                        _lastSelectedFeedChannelItemWithFeed =
                            feedRepository.getFeedChannelItemWithFeed(id)
                        val readLater = _lastSelectedFeedChannelItemWithFeed?.readLater?.toBoolean()
                        //_readLaterStatus.postValue(readLater)

                        when (readLater) {
                            true -> _snackBarMessage.postValue(R.string.msg_marked_for_read_later)
                            false -> _snackBarMessage.postValue(R.string.msg_unmarked_for_read_later)
                        }

                        Timber.d(
                            "[Timber] (GET) feedRepository.updateItemReadStatus(id=%d, readLaterD=%b)",
                            id,
                            readLater
                        )
                    } catch (e: Exception) {
                        Timber.d("[Timber] updateItemReadLaterStatus.Exception: %s.", e.localizedMessage)
                    }
                }
            }
        }
    }

    /**
     * Actualiza el estado del flag "read" del item elegido
     */
    fun updateMarkFeedAsRead(linkName: String) {

        Timber.d("[Timber] updateMarkFeedAsRead(%s)", linkName)

        //if (id != null) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val feed = feedRepository.getFeedWithLinkName(linkName)

                    if (feed != null) {
                        feedRepository.updateFeedReadStatus(feed.id)
                        _snackBarMessage.postValue(R.string.msg_marked_as_read)
                    }
                }
            }
        //}
    }

    /**
     * Actualiza el estado del flag "read" del grupo elegido
     */
    fun updateMarkGroupAsRead(groupName: String) {

        Timber.d("[Timber] updateMarkGroupAsRead(%s)", groupName)

        //if (id != null) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val groupId = feedRepository.getGroupIdByName(groupName)

                if (groupId != null) {
                    feedRepository.updateGroupFeedReadStatus(groupId)
                    _snackBarMessage.postValue(R.string.msg_marked_as_read)
                }
            }
        }
        //}
    }


    /**
     * Actualiza el estado del flag "read" del item elegido
     */
    fun updateFeedFavoriteState(linkName: String, favorite: Boolean) {

        Timber.d("[Timber] updateFeedFavoriteState(%s)", linkName)

        //if (id != null) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val feed = feedRepository.getFeedWithLinkName(linkName)

                if (feed != null) {
                    feedRepository.updateFeedFavoriteState(feed.id, favorite)
                    if (favorite) {
                        _snackBarMessage.postValue(R.string.msg_added_to_favorites)
                    } else {
                        _snackBarMessage.postValue(R.string.msg_removed_from_favorites)
                    }
                }
            }
        }
        //}
    }


    /**
     * Obtiene el Feed con el nombre (linkName) indicado
     */
    suspend fun getFeedWithLinkName(linkName: String): DomainFeed {
        Timber.d("[Timber] getFeedWithLinkName(%s)", linkName)

        lateinit var feed: DomainFeed
        //if (id != null) {
        //viewModelScope.launch {
            feed = withContext(Dispatchers.IO) {
                return@withContext feedRepository.getFeedWithLinkName(linkName)
            }
        //}
        return feed
        //}
    }

    /**
     * Obtiene los feeds de la red y actualiza la BBDD
     */
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

    /**
     * Actualiza los items del feed elegido y lo configura como elegido asignando su apiBaseUrl LiveData
     * para que su observer actualice la pantalla
     */
    fun getFeedWithLinkNameAndSetApiBaseUrl(linkName: String) {
        viewModelScope.launch {
            val feed = withContext(Dispatchers.IO) {
                feedRepository.getFeedWithLinkName(linkName)
            }
            Timber.d(
                "[Timber] feedRepository.getFeedWithLinkName(%s) = %s",
                linkName,
                feed.toString()
            )

            // Cuando se clickea un feed, configura su linkName LiveData para que notifique a su observer
            setSelectedFeedOptions(SelectedFeedOptions().also { it.setLinkNameValue(linkName) })

            _apiBaseUrl.value = feed.link
        }
    }


    /**
     * Se actualiza el _selectedFeedOptions LiveData para que se notifique a su observer
     * y filtre automáticamente la lista de feeds.
     */
    fun setSelectedFeedOptions(selectedFeedOptions: SelectedFeedOptions) {
        Timber.d(
            "[Timber] MainSharedViewModel.setSelectedFeed(DomainFeed.linkName = '%s')",
            selectedFeedOptions.linkName
        )
        _selectedFeedOptions.value = selectedFeedOptions
    }

    /**
     * Configura el ID del regitro elegido desde el layout xml, que a su vez actualiza
     * la variable local del registro seleccionado.
     */
    fun setSelectedFeedChannelItemId(id: Long) {
        viewModelScope.launch {
            _selectedFeedChannelItemId.value = id
            updateItemReadStatus(true)
            _lastSelectedFeedChannelItemWithFeed = feedRepository.getFeedChannelItemWithFeed(id)
            _navigateToContents.value = true
        }
    }

    /**
     * Actualiza la variable _lastSelectedFeedChannelItemWithFeed con todos los datos
     * actualizados del último registro seleccionado
     */
    fun setLastSelectedFeedChannelItemWithFeed(feedChannelItemWithFeed: DomainFeedChannelItemWithFeed) {
        _lastSelectedFeedChannelItemWithFeed = feedChannelItemWithFeed
        Timber.d("[Timber] setLastSelectedFeedChannelItemWithFeed()")
    }

    /**
     * Actualiza el flag "read" de acuerdo al valor indicado para filtrar los datos mostrados
     */
    fun setSelectedFeedOptionsReadFlag(read: Boolean) {
        // Sólo se hace la actualización si el dato realmente cambió
        if (selectedFeedOptions.value != null && selectedFeedOptions.value!!.read != read) {
            setSelectedFeedOptions(SelectedFeedOptions().also { it.read = read })
        }
    }

    /**
     * Actualiza el flag "favorite" de acuerdo al valor indicado para filtrar los datos mostrados
     */
    fun setSelectedFeedOptionsFavoriteTrue() {
        // Sólo se hace la actualización si el dato realmente cambió
        if (selectedFeedOptions.value != null && !selectedFeedOptions.value!!.favorite) {
            setSelectedFeedOptions(SelectedFeedOptions().also { it.setFavoriteTrue() })
        }
    }

    /**
     * Actualiza el flag "readLater" de acuerdo al valor indicado para filtrar los datos mostrados
     */
    fun setSelectedFeedOptionsReadLaterTrue() {
        // Sólo se hace la actualización si el dato realmente cambió
        if (selectedFeedOptions.value != null && !selectedFeedOptions.value!!.readLater) {
            setSelectedFeedOptions(SelectedFeedOptions().also { it.setReadLaterTrue() })
        }
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


    class Factory(private val feedRepository: FeedRepository) :
        ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            Timber.d(
                "[Timber] Factory() - mainSharedViewModel.fragmento: %s",
                MainSharedViewModel::testigo
            )
            return MainSharedViewModel(feedRepository) as T
        }
    }
}
