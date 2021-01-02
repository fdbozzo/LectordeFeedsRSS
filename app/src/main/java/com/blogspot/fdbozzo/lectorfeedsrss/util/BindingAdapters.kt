package com.blogspot.fdbozzo.lectorfeedsrss.util

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.blogspot.fdbozzo.lectorfeedsrss.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.color.MaterialColors.getColor
import com.squareup.picasso.Picasso
import timber.log.Timber

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

@BindingAdapter("textColor")
fun setTextColor(view: TextView, read: Int) {
    when(read){
        1 -> {
            // LEIDO
            view.setTextColor(Color.GRAY)
            //val color = MaterialColors.getColor(view, R.attr.materialThemeOverlay, Color.BLACK)
            //view.setTextColor(color)
        }
        0 -> {
            // NO LEIDO
            view.setTextColor(Color.BLACK)
            //val color = MaterialColors.getColor(view, R.attr.itemTextColor, Color.BLACK)
            //view.setTextColor(color)
        }
    }
}