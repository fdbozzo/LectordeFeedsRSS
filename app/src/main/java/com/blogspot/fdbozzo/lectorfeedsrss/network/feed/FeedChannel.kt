package com.blogspot.fdbozzo.lectorfeedsrss.network.feed

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import java.util.*
import kotlin.collections.ArrayList

@Root(name = "channel", strict = false)
data class FeedChannel(

    var id: Long = 0L,

    var feedId: Long = 0L,

    @field:Element(name = "title")
    var title: String = "",

    @field:Element(name = "description")
    var description: String = "",

    var copyright: String = "",

    @field:ElementList(name = "link", inline = true)
    var links: List<RssLink> = ArrayList(),

    var pubDate: Date = Date(),

    @field:ElementList(name = "item", inline = true, required = false)
    var channelItems: List<FeedChannelItem>? = null

) {
    /**
     * "link" no se puede leer directamente, ya que cualquier link dentro de channel
     * es incluido en una lista de links, de la que luego hay que separar el que interesa,
     * que es el que tiene el link en el campo "text"
     */
    var link = ""
}
