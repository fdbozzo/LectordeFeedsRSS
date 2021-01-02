package com.blogspot.fdbozzo.lectorfeedsrss.data.domain

data class SelectedFeedOptions(
    var linkName: String = "%",
    var favorite: Int = 0,
    var read: Int = 1,
    var readLater: Int = 0
) {
    fun setLinkNameValue(linkName: String) {
        this.linkName = linkName
        this.favorite = 0
        this.readLater = 0
    }

    fun setFavoriteTrue() {
        this.linkName = "%"
        this.favorite = 1
        this.readLater = 0
    }

    fun setReadLaterTrue() {
        this.linkName = "%"
        this.favorite = 0
        this.readLater = 1
    }

}
