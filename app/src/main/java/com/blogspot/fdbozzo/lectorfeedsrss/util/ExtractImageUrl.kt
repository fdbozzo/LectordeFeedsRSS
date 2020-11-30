package com.blogspot.fdbozzo.lectorfeedsrss.util

import java.util.regex.Matcher
import java.util.regex.Pattern

fun getSrcImage(text: String): String {
    val regex = "(src=\"([^\"]|\"\")*\")"
    val p: Pattern = Pattern.compile(regex)
    val m: Matcher = p.matcher(text)
    var urlStr = ""

    if (m.find()) {
        urlStr = m.group()
        urlStr = urlStr.substring(5, urlStr.length - 1)
    }
    return urlStr
}
