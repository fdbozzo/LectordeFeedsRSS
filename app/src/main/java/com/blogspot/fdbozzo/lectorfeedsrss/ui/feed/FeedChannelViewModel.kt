package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed

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

enum class RssApiStatus { LOADING, ERROR, DONE }

class FeedChannelViewModel(private val feedRepository: FeedRepository) : ViewModel() {
//class FeedChannelViewModel(private val daoChannel: FeedChannelItemDao) : ViewModel() {

    //private val channelItemDao: FeedChannelItemDao = daoChannel
    private lateinit var rssApiResponse: RssResponse<Feed>

    private var _items = MutableLiveData<List<DomainFeedChannelItem>>()
    val items: LiveData<List<DomainFeedChannelItem>>
        get() = _items

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


    /**
     * Llamar a getRssFeedData() en el init para obtener los datos inmediatamente.
     */
    init {
        Timber.d("init")
        //items = channelItemDao.getAllFeedChannelItems()
        //getRssFeedData()
        //_items = feedRepository.getFeeds() /** TODO: CUIDADO!, ESTÁ DEVOLVIENDO DESDE BBDD **/
        viewModelScope.launch {
            rssApiResponse = feedRepository.checkNetworkFeeds()

            when (rssApiResponse) {
                is RssResponse.Success -> {

                    val domainFeed = (rssApiResponse as RssResponse.Success<Feed>).data.toDomainFeed()
                    //val domainFeedChannels = (rssApiResponse as RssResponse.Success<Feed>).data.channel.toDomainFeedChannel()

                    // Reemplazo algunos datos
                    //domainFeed.linkName = domainFeedChannels.title
                    //domainFeed.link = domainFeedChannels.link

                    val domainFeedChannelItems = (rssApiResponse as RssResponse.Success<Feed>).data.channel.channelItems?.map {
                        it.toDomainFeedChannelItem()
                    }

                    /** Con la respuesta ahora puedo guardar en BBDD */
                    feedRepository.saveNetworkFeeds(domainFeed)

                    // TODO: Falta filtrar los items leidos antes de actualizar el LiveData
                    _items.value = domainFeedChannelItems

                }
                is RssResponse.Error -> {
                    Timber.d("RssResponse.Error = ${(rssApiResponse as RssResponse.Error).exception.message}")
                }
            }
        }
    }

    /**
     * Setear el estado de LiveData al estado de la API RssApi.
     */
    /**
    private fun getRssFeedData(): Unit {
        /*
            viewModelScope.launch {
                try {
                    _feeds.value = RssApi.retrofitService.getRss()
                    _status.value = RssApiStatus.DONE

                } catch (e: Exception) {
                    _status.value = RssApiStatus.ERROR
                }

            }

         */

        viewModelScope.launch {
            Timber.d("Usando Corrutinas")
            //val response = service.getPosts()
            //val response = service.getRss()
            val response = RssApi.retrofitService.getRss()

            try {
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {

                            // Actualizar la BBDD
                            /*
                            _items = Transformations.map(database.videoDao.getVideos()) {
                                it.asDomainModel()
                            }

                             */




                            val articles = it.channel!!.channelItems

                            if (articles != null) {
                                Timber.d(articles.size.toString())
                                for (i in articles.indices) {
                                    Timber.d("index %d %s", i, articles[i].title)
                                    Timber.d("index %d %s", i, articles[i].link)
                                    Timber.d("index %d %s", i, articles[i].pubDate)
                                    //Timber.d("index %d %s", i, articles[i].description)
                                    //Timber.d("index %d %s", i, articles[i].guid)
                                }
                                /** ACTUALIZAR EL ORIGEN DE DATOS (ITEMS) */
                                //initRecyclerView(articles)
                                //items_normales = articles
                                Timber.d("actualizando LiveData...")
                                //_items.value = articles     // Dispara evento de cambio!
                                Timber.d("LiveData actualizado!")
                                _status.value = RssApiStatus.DONE
                            }
                        }
                    } else {
                        Timber.d("Error network operation failed with ${response.code()}")
                        _status.value = RssApiStatus.ERROR
                    }
                }

            } catch (e: HttpException) {
                Timber.d("Exception ${e.message}")
                _status.value = RssApiStatus.ERROR

            } catch (e: Throwable) {
                Timber.d("Ooops: Something else went wrong")
                _status.value = RssApiStatus.ERROR
            }
        }

    }
    */

}