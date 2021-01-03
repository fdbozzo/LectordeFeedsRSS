package com.blogspot.fdbozzo.lectorfeedsrss.data.domain

data class SelectedFeedOptions(
    var linkName: String = "%",
    var favorite: Boolean = false,
    var read: Boolean = true,
    var readLater: Boolean = false
) {
    fun setLinkNameValue(linkName: String) {
        this.linkName = linkName
        this.favorite = false
        this.readLater = false
    }

    fun setFavoriteTrue() {
        this.linkName = "%"
        this.favorite = true
        this.readLater = false
    }

    fun setReadLaterTrue() {
        this.linkName = "%"
        this.favorite = false
        this.readLater = true
    }

}
