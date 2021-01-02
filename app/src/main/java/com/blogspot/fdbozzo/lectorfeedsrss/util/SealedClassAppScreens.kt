package com.blogspot.fdbozzo.lectorfeedsrss.util

sealed class SealedClassAppScreens {
    class MainActivity: SealedClassAppScreens()
    class FeedChannelFragment: SealedClassAppScreens()
    class ContentsFragment: SealedClassAppScreens()
}
