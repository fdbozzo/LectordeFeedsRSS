package com.blogspot.fdbozzo.lectorfeedsrss.network.feed

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Root(name = "link", strict = false)
data class RssLink (
        // for <link>http://www.somelink.com/</link>
        @field:Text(required = false)
        var text: String = "",

        // for <atom:link rel="self" href="http://www.someotherlink.com" />
        @field:Attribute(required = false)
        var rel: String = "",

        @field:Attribute(name = "type", required = false)
        var contentType: String = "",

        @field:Attribute(required = false)
        var href: String = ""
)
