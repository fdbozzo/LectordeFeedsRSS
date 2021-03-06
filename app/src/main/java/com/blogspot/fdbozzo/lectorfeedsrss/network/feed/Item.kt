package com.blogspot.fdbozzo.lectorfeedsrss.network.feed

import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Item
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Item as DomainItem

@Root(name = "item", strict = false)
data class Item(

    var id: Long = 0L,

    var feedId: Long = 0L,

    @field:Element(name = "title")
    var title: String = "",

    @field:Element(name = "link")
    var link: String = "",

    @field:Element(name = "pubDate")
    var pubDate: String = "",

    @field:Element(name = "description")
    var description: String = "",

    @field:Element(name = "encoded", required = false)
    var contentEncoded: String = "",

    var read: Int = 0,

    var readLater: Int = 0,

    var imageLink: String = ""

)

/**
 * Mapea los Item a entidades del dominio
 */
fun List<Item>.asDomainModel(): List<DomainItem> {
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
            readLater = it.readLater)
    }
}
