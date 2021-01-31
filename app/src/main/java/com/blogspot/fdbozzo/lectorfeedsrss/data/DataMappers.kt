package com.blogspot.fdbozzo.lectorfeedsrss.data

import com.blogspot.fdbozzo.lectorfeedsrss.util.DateParser.Companion.stringToDate as dpStringToDate
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Feed as RoomFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Channel as RoomChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Item as RoomItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.ItemWithFeed as RoomItemWithFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group as RoomGroup
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed as DomainFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Channel as DomainChannel
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Item as DomainItem
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.ItemWithFeed as DomainItemWithFeed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group as DomainGroup
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Feed as ServerFeed
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Channel as ServerChannel
import com.blogspot.fdbozzo.lectorfeedsrss.network.feed.Item as ServerItem


/** ROOM BBDD **/
fun RoomGroup.toDomainGroup(): DomainGroup =
    DomainGroup(id, groupName)

fun RoomFeed.toDomainFeed(): DomainFeed =
    DomainFeed(id, groupId, linkName, link, favorite)

fun RoomChannel.toDomainChannel(): DomainChannel =
    DomainChannel(id, feedId, title, description, copyright, link, pubDate)

fun RoomItem.toDomainItem(): DomainItem =
    DomainItem(id, feedId, title, link, pubDate, description, read, readLater, imageLink)

fun RoomItemWithFeed.toDomainItemWithFeed(): DomainItemWithFeed =
    DomainItemWithFeed(linkName, id, feedId, title, link, pubDate, description, read, readLater, imageLink)


/** DOMAIN **/
fun DomainGroup.toRoomGroup(): RoomGroup =
    RoomGroup(groupName).also {it.id = this.id}

fun DomainFeed.toRoomFeed(): RoomFeed =
    RoomFeed(groupId, linkName, link, favorite).also {it.id = this.id}

fun DomainChannel.toRoomChannel(): RoomChannel =
    RoomChannel(feedId, title, description, copyright, link, pubDate)

fun DomainItem.toRoomItem(): RoomItem =
    RoomItem(feedId, title, link, pubDate, description, read, readLater, imageLink).also {it.id = this.id}

/** SERVER **/
fun ServerFeed.toRoomFeed(): RoomFeed =
    RoomFeed(groupId, linkName, link, favorite)

fun ServerChannel.toRoomChannel(): RoomChannel =
    RoomChannel(feedId, title, description, copyright, link, pubDate)

fun ServerItem.toRoomItem(): RoomItem =
    RoomItem(
        feedId,
        title,
        link,
        dpStringToDate(pubDate, "EEE, d MMM yyyy HH:mm:ss Z"),
        description,
        read,
        readLater,
        imageLink)

fun ServerFeed.toDomainFeed(): DomainFeed =
    DomainFeed(id, groupId, linkName, link, favorite, channel.toDomainChannel(), version)

fun ServerChannel.toDomainChannel(): DomainChannel =
    DomainChannel(id, feedId, title, description, copyright,
        link = if (this.links.isNotEmpty()) (this.links[this.links.size - 1].text) else "https://nonexistent.com",
        pubDate,
        (items as List<ServerItem>).map {
            it.toDomainItem()
        })

fun ServerItem.toDomainItem(): DomainItem =
    DomainItem(
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
 * Mapea los Channel de la base de datos a entidades del dominio
 */
fun List<RoomChannel>.asDomainModelChannel(): List<DomainChannel> {
    return map {
        DomainChannel(
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
 * Mapea los Channel del dominio a entidades de la base de datos
 */
fun List<DomainChannel>.asDatabaseModelChannel(): List<RoomChannel> {
    return map {
        RoomChannel(
            title = it.title,
            description = it.description,
            feedId = it.feedId,
            pubDate = it.pubDate,
            link = it.link
        )
    }
}

/**
 * Mapea los Item de la base de datos a entidades del dominio
 */
fun List<RoomItem>.asDomainModelItem(): List<DomainItem> {
    return map {
        DomainItem(
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
 * Mapea los Item del dominio a entidades de la base de datos
 */
fun List<DomainItem>.asDatabaseModelItem(): List<RoomItem> {
    return map {
        RoomItem(
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
