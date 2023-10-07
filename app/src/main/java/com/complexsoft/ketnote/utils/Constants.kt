package com.complexsoft.ketnote.utils

import java.text.SimpleDateFormat
import java.util.Locale

object Constants {
    const val APP_ID = "ketnoteapp-punhn"
    const val WEB_CLIENT =
        "584951843971-pl6j9brdsqo2160tn3798oubc8126le7.apps.googleusercontent.com"
}

fun Long.toHumanDate():String {
    val format = SimpleDateFormat("yyyy.dd.MM HH:mm", Locale.ENGLISH)
    return format.format(this)
}