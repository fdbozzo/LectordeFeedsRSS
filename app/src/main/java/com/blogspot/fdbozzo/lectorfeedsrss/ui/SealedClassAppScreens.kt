package com.blogspot.fdbozzo.lectorfeedsrss.ui

sealed class SealedClassAppScreens {
    object MainActivity : SealedClassAppScreens()
    object FeedChannelFragment : SealedClassAppScreens()
    object ContentsFragment : SealedClassAppScreens()
    object SettingsFragment : SealedClassAppScreens()
    object AddGroupFragment : SealedClassAppScreens()
    object AddFeedFragment : SealedClassAppScreens()
    object EditGroupFragment : SealedClassAppScreens()
    object LoginFragment : SealedClassAppScreens()
}
