package com.blogspot.fdbozzo.lectorfeedsrss.ui

sealed class SealedClassAppScreens {
    object MainActivity : SealedClassAppScreens()
    object FeedChannelFragment : SealedClassAppScreens()
    object ContentsFragment : SealedClassAppScreens()
    object SettingsFragment : SealedClassAppScreens()
}
