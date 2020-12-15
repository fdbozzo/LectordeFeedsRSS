package com.blogspot.fdbozzo.lectorfeedsrss.data

sealed class RssResponse<out T: Any> {
    data class Success<out T : Any>(val data: T) : RssResponse<T>()
    data class Error(val exception: Exception) : RssResponse<Nothing>()
}