package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed.channel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.Feed
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.item.FeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.database.feed.channel.item.FeedChannelItemDao
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber

enum class RssApiStatus { LOADING, ERROR, DONE }

class FeedChannelViewModel(private val daoChannel: FeedChannelItemDao) : ViewModel() {

    //private val channelItemDao: FeedChannelItemDao = daoChannel

    private val _items = MutableLiveData<List<FeedChannelItem>>()
    val items: LiveData<List<FeedChannelItem>>
        get() = _items

    private val _feeds = MutableLiveData<Response<Feed>>()
    val feeds: LiveData<Response<Feed>>
        get() = _feeds

    private val _status = MutableLiveData<RssApiStatus>()
    val status: LiveData<RssApiStatus>
        get() = _status


    /**
     * Llamar a getRssFeedData() en el init para obtener los datos inmediatamente.
     */
    init {
        //items = channelItemDao.getAllFeedChannelItems()
        getRssFeedData()
        Timber.d("init")
    }

    /**
     * Setear el estado de LiveData al estado de la API RssApi.
     */
    private fun getRssFeedData() {
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
                                _items.value = articles     // Dispara evento de cambio!
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

}