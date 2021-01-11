package com.blogspot.fdbozzo.lectorfeedsrss.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

// Convierte true -> 1 y false -> 0
fun Boolean.toInt() = if (this) 1 else 0

// Convierte 1 -> true y 0 -> false
fun Int.toBoolean() = this == 1

/**
 * Forzar la finalización con el string indicado
 */
fun String.forceEndingWithChar(char: String): String {
    // Comprobar si el string tiene el carácter indicado al final
    return if (!this.endsWith(char)) {
        "${this}${char}"
    } else {
        this
    }
}

/**
 * Forzar el inicio con el string indicado
 */
fun String.forceStartingWithString(string: String): String {
    // Comprobar si el string tiene el string indicado al inicio
    return if (!this.startsWith(string)) {
        "${string}${this}"
    } else {
        this
    }
}

/**
 * Forzar que la cadena no termine con el string indicado, quitándolo si es necesario
 */
fun String.forceNotEndingWithString(string: String): String {
    // Comprobar si el string tiene el string indicado al final
    return if (this.endsWith(string)) {
        this.substring(0, this.length - string.length)
    } else {
        this
    }
}


// Permite devolver LiveData de un dato único sólo cuando cambia (1 registro sólo)
fun <T> LiveData<T>.getDistinct(): LiveData<T> {
    val distinctLiveData = MediatorLiveData<T>()
    distinctLiveData.addSource(this, object : Observer<T> {
        private var initialized = false
        private var lastObj: T? = null
        override fun onChanged(obj: T?) {
            if (!initialized) {
                initialized = true
                lastObj = obj
                distinctLiveData.postValue(lastObj)
            } else if ((obj == null && lastObj != null) || obj != lastObj) {
                lastObj = obj
                distinctLiveData.postValue(lastObj)
            }
        }
    })
    return distinctLiveData
}
