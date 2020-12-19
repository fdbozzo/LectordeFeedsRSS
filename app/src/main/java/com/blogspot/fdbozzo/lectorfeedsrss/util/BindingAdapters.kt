package com.blogspot.fdbozzo.lectorfeedsrss.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso

/**
 * Binding adapter
 * Load image from url using Picasso library
 */
/*
@BindingAdapter("imageUrl")
fun bindImageUrl(view: ImageView, description: String?) {
    if (!description.isNullOrEmpty()) {
        // Obtengo la URL de la imagen de la descripción (si hay una)
        val imagen = getSrcImage(description)
        if (!imagen.isEmpty()) {
            Picasso.get().load(imagen).into(view)
        }
    }
}
 */

@BindingAdapter("imageUrl")
fun bindImageUrl(view: ImageView, imageLink: String?) {
    if (!imageLink.isNullOrEmpty()) {
        Picasso.get().load(imageLink).into(view)
    }
}
