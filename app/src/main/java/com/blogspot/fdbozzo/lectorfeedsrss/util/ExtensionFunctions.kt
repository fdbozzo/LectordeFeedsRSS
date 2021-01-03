package com.blogspot.fdbozzo.lectorfeedsrss.util

// Convierte true -> 1 y false -> 0
fun Boolean.toInt() = if (this) 1 else 0

// Convierte 1 -> true y 0 -> false
fun Int.toBoolean() = this == 1

