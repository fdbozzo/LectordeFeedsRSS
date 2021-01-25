package com.blogspot.fdbozzo.lectorfeedsrss

import androidx.lifecycle.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.SelectedFeedOptions
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssApiStatus
import com.blogspot.fdbozzo.lectorfeedsrss.ui.SealedClassAppScreens
import com.blogspot.fdbozzo.lectorfeedsrss.util.forceEndingWithChar
import com.blogspot.fdbozzo.lectorfeedsrss.util.forceNotEndingWithString
import com.blogspot.fdbozzo.lectorfeedsrss.util.forceStartingWithString
import com.blogspot.fdbozzo.lectorfeedsrss.util.toBoolean
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannel as DomainFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItemWithFeed as DomainFeedChannelItemWithFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group as DomainGroup
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as NetworkFeed

class MainSharedViewModel(val feedRepository: FeedRepository) : ViewModel() {

    var testigo: String? = ""

    var vmInicializado = false

    private lateinit var rssApiResponse: RssResponse<NetworkFeed>

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
     * Conecta el LiveData "items" a la consulta de BBDD para actualización automática cuando cambien
     * los valores del filtro LiveData "selectedFeedOptions"
     */
    val items: LiveData<List<DomainFeedChannelItemWithFeed>?> =
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
        viewModelScope.launch {
            // Cargamos datos iniciales en el drawer
            setupInitialDrawerMenuData()

            // Cargar todos los feeds actualizados
            refreshActiveFeeds()
        }

    }

    suspend fun refreshActiveFeeds() {
        val valSelectedFeedOptions = selectedFeedOptions.value
        val linkName = selectedFeedOptions.value?.linkName ?: "%"

        if (valSelectedFeedOptions != null) {
            when {
                valSelectedFeedOptions.readLater -> {
                    // Read Later - No actualizar porque son noticias estáticas preseleccionadas
                    Timber.d("[Timber] No se recarga por ser Read Later")
                }
                valSelectedFeedOptions.favorite -> {
                    // Favorites - Actualizar
                    feedRepository.getAllFeeds()?.forEach {
                        if (it.favorite == 1) {
                            Timber.d("[Timber] Lanzar carga del feed %s", it.link)
                            viewModelScope.launch { getFeedsFromUrl(it.link) }
                        } else {
                            Timber.d("[Timber] Feed %s no se carga por no ser Favorito", it.link)
                        }
                    }
                }
                linkName == "%" -> {
                    // All feeds
                    feedRepository.getAllFeeds()?.forEach {
                        Timber.d("[Timber] Lanzar carga del feed %s", it.link)
                        viewModelScope.launch { getFeedsFromUrl(it.link) }
                    }

                }
                else -> {
                    // Un feed en particular (linkName contiene su nombre)
                    Timber.d("[Timber] Lanzar carga del feed %s", linkName)
                    feedRepository.getFeedByLinkName(linkName)
                }
            }
        }
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

    fun setSnackBarMessage(stringRId: Int) {
        _snackBarMessage.value = stringRId
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
                feedRepository.updateReadStatus(id, read)
            }
        }
    }

    /**
     * Actualiza el estado del flag "read_later" del item elegido
     */
    fun updateItemReadLaterStatus(id: Long? = selectedFeedChannelItemId.value) {
        if (id != null) {
            viewModelScope.launch {
                try {
                    feedRepository.updateInverseReadLaterStatus(id)
                    val feed = feedRepository.getFeedChannelItemWithFeed(id)
                    val readLater = feed?.readLater?.toBoolean()

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
                    Timber.d(
                        "[Timber] updateItemReadLaterStatus.Exception: %s.",
                        e.localizedMessage
                    )
                }
            }
        }
    }

    /**
     * Actualiza el estado del flag "read" del item elegido
     */
    fun updateMarkFeedAsRead(linkName: String) {

        Timber.d("[Timber] updateMarkFeedAsRead(%s)", linkName)

        viewModelScope.launch {
            val feed = feedRepository.getFeedByLinkName(linkName)

            if (feed != null) {
                feedRepository.updateReadStatus(feed.id, true)
                _snackBarMessage.postValue(R.string.msg_marked_as_read)
            }
        }
    }

    /**
     * Actualiza el estado del flag "read" del grupo elegido
     */
    fun updateMarkGroupAsRead(groupName: String) {
        Timber.d("[Timber] updateMarkGroupAsRead(%s)", groupName)

        viewModelScope.launch {
            val groupId = feedRepository.getGroupIdByName(groupName)

            if (groupId != null) {
                feedRepository.updateGroupFeedReadStatus(groupId)
                _snackBarMessage.postValue(R.string.msg_marked_as_read)
            }
        }
    }


    /**
     * Actualiza el estado del flag "read" de todos los items
     */
    fun updateMarkAllFeedAsRead() {
        Timber.d("[Timber] updateMarkAllFeedAsRead()")

        viewModelScope.launch {
            feedRepository.updateMarkAllFeedAsRead()
            _snackBarMessage.postValue(R.string.msg_marked_as_read)
        }
    }

    /**
     * Actualiza el estado del flag "read" del item elegido
     */
    fun updateFeedFavoriteState(linkName: String, favorite: Boolean) {
        Timber.d("[Timber] updateFeedFavoriteState(%s)", linkName)

        viewModelScope.launch {
            val feed = feedRepository.getFeedByLinkName(linkName)

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


    /**
     * Actualiza el estado del flag "read" del item elegido
     */
    fun deleteFeed(linkName: String) {
        viewModelScope.launch {
            val feed = feedRepository.getFeedByLinkName(linkName)

            if (feed != null) {
                val deleted = feedRepository.deleteFeed(feed)

                if (deleted > 0) {
                    _snackBarMessage.postValue(R.string.msg_source_removed)
                } else {
                    _snackBarMessage.postValue(R.string.msg_operation_not_executed)
                }

                Timber.d("[Timber] deleteFeed(%s): id=%d, deleted=%d", linkName, feed.id, deleted)
            }
        }
    }

    /**
     * Actualiza el estado del flag "read" del item elegido
     */
    fun addGroup(group: DomainGroup) {
        viewModelScope.launch {
            val saved = feedRepository.saveLocalGroup(group)
            if (saved > 0) {
                _snackBarMessage.postValue(R.string.msg_group_added)
            } else {
                _snackBarMessage.postValue(R.string.msg_value_already_exists)
            }

            Timber.d("[Timber] addGroup(%s)", group)
        }
    }


    fun updateGroup(group: DomainGroup) {
        viewModelScope.launch {
            val saved = feedRepository.updateGroup(group)
            if (saved > 0) {
                _snackBarMessage.postValue(R.string.msg_group_updated)
            } else {
                _snackBarMessage.postValue(R.string.msg_operation_not_executed)
            }

            Timber.d("[Timber] updateGroup(%s)", group)
        }
    }


    /**
     * Actualiza el estado del flag "read" del item elegido
     */
    fun deleteGroup(linkName: String) {
        viewModelScope.launch {
            var deleted = 0
            deleted = when (linkName) {
                Group.DEFAULT_NAME -> 0
                else -> feedRepository.deleteGroupByName(linkName)
            }

            if (deleted > 0) {
                _snackBarMessage.postValue(R.string.msg_group_removed)
            } else {
                _snackBarMessage.postValue(R.string.msg_operation_not_executed)
            }

            Timber.d("[Timber] deleteGroup(%s): deleted=%d", linkName, deleted)
        }
    }


    /**
     * Obtiene el Feed con el nombre (linkName) indicado
     */
    suspend fun getGroupByName(groupName: String): DomainGroup? {
        Timber.d("[Timber] getGroupByName(%s)", groupName)
        val group: DomainGroup? = feedRepository.getGroupByName(groupName)
        return group
    }

    suspend fun getGroups(): List<DomainGroup>? = feedRepository.getGroups()

    /**
     * Obtiene el Feed con el nombre (linkName) indicado
     */
    suspend fun getFeedWithLinkName(linkName: String): DomainFeed? {
        Timber.d("[Timber] getFeedWithLinkName(%s)", linkName)
        val feed: DomainFeed? = feedRepository.getFeedByLinkName(linkName)
        return feed
    }


    /**
     * Se busca el Feed para comprobar si es válido y lo intenta guardar, si no existe
     */
    suspend fun buscarFeedComprobarSiEsValidoYGuardar(link: String, group: DomainGroup): RssResponse<NetworkFeed> {
        var valApiBaseUrl = ""

        /**
         * Normalizar el link como URL válida
         */
        if (link.startsWith("http://") || link.startsWith("https://")) {
            // Si se indica http o https, se respeta
            valApiBaseUrl = link.forceEndingWithChar("/")
        } else {
            // Si no se indica http o https, se fuerza https
            valApiBaseUrl = link.forceStartingWithString("https://").forceEndingWithChar("/")
        }

        val valApiBaseUrl2 = valApiBaseUrl.forceNotEndingWithString("/")

        try {

            /**
             * Confirmar si no está ya cargada
             */
            if (feedRepository.getFeedIdByLink(valApiBaseUrl) != null ||
                feedRepository.getFeedIdByLink(valApiBaseUrl2) != null)
                throw Exception("Feed already exists")

            Timber.d("[Timber] valApiBaseUrl = '%s' , normalizada = %s", valApiBaseUrl, valApiBaseUrl.forceNotEndingWithString("/"))


            /**
             * Hacer la consulta de la URL a la red
             */
            rssApiResponse = feedRepository.checkNetworkFeeds(valApiBaseUrl, group.id)

            when (rssApiResponse) {
                is RssResponse.Success -> {
                    setSnackBarMessage(R.string.msg_feed_added)
                }
                is RssResponse.Error -> {
                    // Mostrar mensaje error
                    Timber.d((rssApiResponse as RssResponse.Error).exception, "[Timber] AddFeedFragment.buscarFeedObtenerInfoYGuardar --> RssResponse.Error")
                    Timber.d("[Timber] AddFeedFragment.buscarFeedObtenerInfoYGuardar --> RssResponse.Error = ${(rssApiResponse as RssResponse.Error).exception.message}")
                    //throw Exception((rssApiResponse as RssResponse.Error).exception.message)
                }
            }

            return rssApiResponse

        } catch (e: Exception) {
            Timber.d(e, "[Timber] AddFeedFragment.buscarFeedObtenerInfoYGuardar --> Error")
            throw e
        }
    }


    /**
     * Obtiene los feeds de la red y actualiza la BBDD
     */
    fun getFeedsFromUrl(urlParam: String = "", showErrMsg: Boolean = false) {
        Timber.d("[Timber] MainSharedViewModel.getFeeds() - Obtener noticias de  ${_apiBaseUrl.value}")
        var url = urlParam

        if (url.isBlank()) {
            if (apiBaseUrl.value.isNullOrBlank()) {
                return
            } else {
                url = apiBaseUrl.value!!
            }
        }

        viewModelScope.launch {
            rssApiResponse = feedRepository.checkNetworkFeeds(url)

            when (rssApiResponse) {
                is RssResponse.Success -> {
                    // TODO: Falta filtrar los items leidos antes de actualizar el LiveData
                    Timber.d("[Timber] RssResponse.Success!")
                }
                is RssResponse.Error -> {
                    // TODO: Falta controlar errores
                    Timber.d("[Timber] RssResponse.Error = ${(rssApiResponse as RssResponse.Error).exception.message}")
                    val msgError = (rssApiResponse as RssResponse.Error).exception.message

                    when {
                        msgError == null -> {
                            Timber.d("[Timber] msgError es null!")
                            setSnackBarMessage(R.string.msg_error_with_null_message)
                        }
                        !showErrMsg -> {
                            Timber.d("[Timber] msgError se ignora")
                        }
                        msgError.contains("code=403") -> {
                            Timber.d("[Timber] msgError es 403!")
                            setSnackBarMessage(R.string.msg_error_network_operation_failed_with_403)
                        }
                        else -> {
                            Timber.d("[Timber] msgError es otro")
                            setSnackBarMessage(R.string.msg_error_with_some_unprogrammed_message)
                        }
                    }
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
            val feed = feedRepository.getFeedByLinkName(linkName)
            Timber.d(
                "[Timber] feedRepository.getFeedWithLinkName(%s) = %s",
                linkName,
                feed.toString()
            )

            // Cuando se clickea un feed, configura su linkName LiveData para que notifique a su observer
            setSelectedFeedOptions(SelectedFeedOptions().also { it.setLinkNameValue(linkName) })

            if (feed != null) {
                _apiBaseUrl.value = feed.link
            }
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
            Timber.d("[Timber] setSelectedFeedChannelItemId(id=%d)", id)
            _selectedFeedChannelItemId.value = id
            updateItemReadStatus(true)
            _lastSelectedFeedChannelItemWithFeed = feedRepository.getFeedChannelItemWithFeed(id)
            _navigateToContents.value = true
        }
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

    private suspend fun setupInitialDrawerMenuData() {

        try {
            // Compruebo si existe el último grupo, y si no existe borro toto y relleno
            val groupId = feedRepository.getGroupIdByName("Hardware")

            if (groupId != null && groupId != 0L) {
                /**
                 * Si groupId != 0 significa que se encontró el grupo buscado,
                 * y que entonces no hay que borrar los datos nuevamente.
                 */
                Timber.d(
                    "[Timber] setupInitialDrawerMenuData() - group.id = %d  --> Ya hay datos",
                    groupId
                )
                return
            }

            Timber.d(
                "[Timber] setupInitialDrawerMenuData() - group.id = %d --> Se recrean las opciones",
                groupId
            )

            // Borro la bbdd
            feedRepository.deleteAllLocalGroups()

            // G1-F1
            var gname = Group.DEFAULT_NAME
            feedRepository.saveLocalGroup(DomainGroup()) // "Uncategorized"
            var gId = feedRepository.getGroupIdByName(gname)
                ?: throw Exception("setupInitialDrawerMenuData [G1-F1]: gId = null")
            var feed = DomainFeed(
                groupId = gId,
                linkName = "Stéphane Graber's website",
                link = "https://stgraber.org"
            )
            feedRepository.saveLocalFeed(feed)
            var fId = feedRepository.getFeedIdByLink(feed.link) ?: throw Exception("feedId es null")
            feedRepository.saveLocalFeedChannel(
                DomainFeedChannel(
                    feedId = fId,
                    title = "Stéphane Graber's website",
                    description = "Stéphane Graber's Feed",
                    link = "https://stgraber.org/feed"
                )
            )

            // G2-F2
            gname = "Tecnología y Ciencia"
            feedRepository.saveLocalGroup(DomainGroup(groupName = gname))
            gId = feedRepository.getGroupIdByName(gname)
                ?: throw Exception("setupInitialDrawerMenuData [G1-F1]: gId = null")
            feed = DomainFeed(
                groupId = gId,
                linkName = "HardZone",
                link = "https://hardzone.es"
            )
            feedRepository.saveLocalFeed(feed)
            fId = feedRepository.getFeedIdByLink(feed.link) ?: throw Exception("feedId es null")
            feedRepository.saveLocalFeedChannel(
                DomainFeedChannel(
                    feedId = fId,
                    title = "HardZone",
                    description = "HardZone Feed",
                    link = "https://hardzone.es"
                )
            )

            // G3-F3
            gname = "Hogar"
            feedRepository.saveLocalGroup(DomainGroup(groupName = gname))
            gId = feedRepository.getGroupIdByName(gname)
                ?: throw Exception("setupInitialDrawerMenuData [G1-F1]: gId = null")
            feed = DomainFeed(
                groupId = gId,
                linkName = "EcoInventos",
                link = "https://ecoinventos.com"
            )
            feedRepository.saveLocalFeed(feed)
            fId = feedRepository.getFeedIdByLink(feed.link) ?: throw Exception("feedId es null")
            feedRepository.saveLocalFeedChannel(
                DomainFeedChannel(
                    feedId = fId,
                    title = "EcoInventos",
                    description = "EcoInventos Feed",
                    link = "https://ecoinventos.com"
                )
            )

            // G4-F4
            gname = "Android"
            feedRepository.saveLocalGroup(DomainGroup(groupName = gname))
            gId = feedRepository.getGroupIdByName(gname)
                ?: throw Exception("setupInitialDrawerMenuData [G1-F1]: gId = null")
            feed = DomainFeed(
                groupId = gId,
                linkName = "Android Police",
                link = "https://www.androidpolice.com"
            )
            feedRepository.saveLocalFeed(feed)
            fId = feedRepository.getFeedIdByLink(feed.link) ?: throw Exception("feedId es null")
            feedRepository.saveLocalFeedChannel(
                DomainFeedChannel(
                    feedId = fId,
                    title = "Android Police",
                    description = "Android Police Feed",
                    link = "https://www.androidpolice.com"
                )
            )

            // G4-F4
            gname = "Hardware"
            feedRepository.saveLocalGroup(DomainGroup(groupName = gname))
            gId = feedRepository.getGroupIdByName(gname)
                ?: throw Exception("setupInitialDrawerMenuData [G1-F1]: gId = null")
            feed = DomainFeed(
                groupId = gId,
                linkName = "El Chapuzas Informático",
                link = "https://elchapuzasinformatico.com"
            )
            feedRepository.saveLocalFeed(feed)
            fId = feedRepository.getFeedIdByLink(feed.link) ?: throw Exception("feedId es null")
            feedRepository.saveLocalFeedChannel(
                DomainFeedChannel(
                    feedId = fId,
                    title = "El Chapuzas Informático",
                    description = "El Chapuzas Informático Feed",
                    link = "https://elchapuzasinformatico.com"
                )
            )

        } catch (e: Exception) {
            Timber.d(e, "[Timber] MainSharedViewModel.setupInitialDrawerMenuData() - ERROR")
        }
    }

    /**
     * Lista de valores del menú Drawer
     */
    val menuData: LiveData<HashMap<String, List<String>>> =
        feedRepository.getGroupsWithFeeds().asLiveData()


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
