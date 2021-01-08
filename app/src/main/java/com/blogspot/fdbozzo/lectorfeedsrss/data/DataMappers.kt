package com.blogspot.fdbozzo.lectorfeedsrss.data

import timber.log.Timber
import com.blogspot.fdbozzo.lectorfeedsrss.util.DateParser.Companion.stringToDate as dpStringToDate
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Feed as RoomFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedChannel as RoomFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedChannelItem as RoomFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.FeedChannelItemWithFeed as RoomFeedChannelItemWithFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group as RoomGroup
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannel as DomainFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItem as DomainFeedChannelItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.FeedChannelItemWithFeed as DomainFeedChannelItemWithFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group as DomainGroup
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.FeedChannel as ServerFeedChannel
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.FeedChannelItem as ServerFeedChannelItem


/** ROOM BBDD **/
fun RoomGroup.toDomainGroup(): DomainGroup =
    DomainGroup(id, groupName)

fun RoomFeed.toDomainFeed(): DomainFeed =
    DomainFeed(id, groupId, linkName, link, favorite)

fun RoomFeedChannel.toDomainFeedChannel(): DomainFeedChannel =
    DomainFeedChannel(id, feedId, title, description, copyright, link, pubDate)

fun RoomFeedChannelItem.toDomainFeedChannelItem(): DomainFeedChannelItem =
    DomainFeedChannelItem(id, feedId, title, link, pubDate, description, read, readLater, imageLink)

fun RoomFeedChannelItemWithFeed.toDomainFeedChannelItemWithFeed(): DomainFeedChannelItemWithFeed =
    DomainFeedChannelItemWithFeed(linkName, id, feedId, title, link, pubDate, description, read, readLater, imageLink)


/** DOMAIN **/
fun DomainGroup.toRoomGroup(): RoomGroup =
    RoomGroup(id, groupName)

fun DomainFeed.toRoomFeed(): RoomFeed =
    RoomFeed(id, groupId, linkName, link, favorite)

fun DomainFeedChannel.toRoomFeedChannel(): RoomFeedChannel =
    RoomFeedChannel(feedId, title, description, copyright, link, pubDate)

fun DomainFeedChannelItem.toRoomFeedChannelItem(): RoomFeedChannelItem =
    RoomFeedChannelItem(id, feedId, title, link, pubDate, description, read, readLater, imageLink)

/** SERVER **/
fun ServerFeed.toRoomFeed(): RoomFeed =
    RoomFeed(id, groupId, linkName, link, favorite)

fun ServerFeedChannel.toRoomFeedChannel(): RoomFeedChannel =
    RoomFeedChannel(feedId, title, description, copyright, link, pubDate)

fun ServerFeedChannelItem.toRoomFeedChannelItem(): RoomFeedChannelItem =
    RoomFeedChannelItem(
        id,
        feedId,
        title,
        link,
        dpStringToDate(pubDate, "EEE, d MMM yyyy HH:mm:ss Z"),
        description,
        read,
        readLater,
        imageLink)

fun ServerFeed.toDomainFeed(): DomainFeed =
    DomainFeed(id, groupId, linkName, link, favorite, channel.toDomainFeedChannel(), version)

fun ServerFeedChannel.toDomainFeedChannel(): DomainFeedChannel =
    DomainFeedChannel(id, feedId, title, description, copyright,
        link = if (this.links.isNotEmpty()) (this.links[this.links.size - 1].text) else "",
        pubDate, (channelItems as List<ServerFeedChannelItem>).map {
            it.toDomainFeedChannelItem()
        })

fun ServerFeedChannelItem.toDomainFeedChannelItem(): DomainFeedChannelItem =
    DomainFeedChannelItem(
        id,
        feedId,
        title,
        link,
        dpStringToDate(pubDate, "EEE, d MMM yyyy HH:mm:ss Z"),
        description,
        read,
        readLater,
        imageLink
    )

/*
/**
 * Mapea los Feed de la base de datos a entidades del dominio
 */
fun List<RoomFeed>.asDomainModelFeed(): List<DomainFeed> {
    return map {
        DomainFeed(
            id = it.id,
            groupId = it.groupId,
            favorite = it.favorite,
            link = it.link
        )
    }
}

/**
 * Mapea los Feed del dominio a entidades de la base de datos
 */
fun List<DomainFeed>.asDatabaseModelFeed(): List<RoomFeed> {
    return map {
        RoomFeed(
            groupId = it.groupId,
            favorite = it.favorite,
            link = it.link
        )
    }
}


/**
 * Mapea los FeedChannel de la base de datos a entidades del dominio
 */
fun List<RoomFeedChannel>.asDomainModelFeedChannel(): List<DomainFeedChannel> {
    return map {
        DomainFeedChannel(
            id = it.id,
            title = it.title,
            description = it.description,
            feedId = it.feedId,
            pubDate = it.pubDate,
            link = it.link
        )
    }
}

/**
 * Mapea los FeedChannel del dominio a entidades de la base de datos
 */
fun List<DomainFeedChannel>.asDatabaseModelFeedChannel(): List<RoomFeedChannel> {
    return map {
        RoomFeedChannel(
            title = it.title,
            description = it.description,
            feedId = it.feedId,
            pubDate = it.pubDate,
            link = it.link
        )
    }
}

/**
 * Mapea los FeedChannelItem de la base de datos a entidades del dominio
 */
fun List<RoomFeedChannelItem>.asDomainModelFeedChannelItem(): List<DomainFeedChannelItem> {
    return map {
        DomainFeedChannelItem(
            id = it.id,
            title = it.title,
            description = it.description,
            feedId = it.feedId,
            pubDate = it.pubDate,
            link = it.link,
            imageLink = it.imageLink,
            read = it.read,
            readLater = it.readLater
        )
    }
}

/**
 * Mapea los FeedChannelItem del dominio a entidades de la base de datos
 */
fun List<DomainFeedChannelItem>.asDatabaseModelFeedChannelItem(): List<RoomFeedChannelItem> {
    return map {
        RoomFeedChannelItem(
            title = it.title,
            description = it.description,
            feedId = it.feedId,
            pubDate = it.pubDate,
            link = it.link,
            imageLink = it.imageLink,
            read = it.read,
            readLater = it.readLater
        )
    }
}

/**
 * Mapea los Group de la base de datos a entidades del dominio
 */
fun List<RoomGroup>.asDomainModel(): List<DomainGroup> {
    return map {
        DomainGroup(
            id = it.id,
            groupName = it.groupName
        )
    }
}

/**
 * Mapea los Group del dominio a entidades  de la base de datos
 */
fun List<RoomGroup>.asDatabaseModel(): List<DomainGroup> {
    return map {
        DomainGroup(
            groupName = it.groupName
        )
    }
}
*/
