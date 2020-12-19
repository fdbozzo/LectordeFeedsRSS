package com.blogspot.fdbozzo.lectorfeedsrss.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso

/**
 * Binding adapter
 * Load image from url using Picasso library
 */

@BindingAdapter("imageUrl")
fun bindImageUrl(view: ImageView, imageLink: String?) {
    if (!imageLink.isNullOrEmpty()) {
        Picasso.get().load(imageLink).into(view)
    }
}
