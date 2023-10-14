package com.complexsoft.ketnote.utils

import android.net.Uri
import com.complexsoft.ketnote.data.model.ImageNote
import java.text.SimpleDateFormat
import java.util.Locale

object Constants {
    const val APP_VERSION = "2310-beta-3 Trinity"
    const val APP_ID = "ketnoteapp-punhn"
    const val WEB_CLIENT =
        "584951843971-pl6j9brdsqo2160tn3798oubc8126le7.apps.googleusercontent.com"
    const val DB_PASSWORD = "Y282lAEZODVckp3j"
    const val IMAGES_DATABASE = "images_db"
    const val IMAGE_TO_UPLOAD_TABLE = "image_to_upload_table"
    const val IMAGE_TO_DELETE_TABLE = "image_to_delete_table"
}

fun Long.toHumanDate(): String {
    val format = SimpleDateFormat("yyyy.dd.MM HH:mm", Locale.ENGLISH)
    return format.format(this)
}

fun List<Uri>.toImageNoteList(): List<ImageNote> {
    return this.map {
        ImageNote(src = it.toString())
    }
}