package com.blogspot.fdbozzo.lectorfeedsrss.data.domain

/**
 * OPCIONES DE FILTRADO
 */
data class SelectedFeedOptions(
    /**
     * All feeds
     */
    var linkName: String = "%",
    var favorite: Boolean = false,
    var read: Boolean = true,
    var readLater: Boolean = false
) {

    /**
     * Feed elegido
     */
    fun setLinkNameValue(linkName: String) {
        this.linkName = linkName
        this.favorite = false
        this.readLater = false
    }

    /**
     * Favoritos
     */
    fun setFavoriteTrue() {
        this.linkName = "%"
        this.favorite = true
        this.readLater = false
    }

    /**
     * ReadLater
     */
    fun setReadLaterTrue() {
        this.linkName = "%"
        this.favorite = false
        this.readLater = true
    }

}
