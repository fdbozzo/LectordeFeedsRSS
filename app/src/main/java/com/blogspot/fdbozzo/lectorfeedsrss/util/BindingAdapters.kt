package com.blogspot.fdbozzo.lectorfeedsrss.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso

/**
 * Binding adapter
 * Load image from url using Picasso library
 */
@BindingAdapter("imageUrl")
fun bindImageUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        Picasso.get().load(imageUrl).into(view)
    }
}