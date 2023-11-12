package com.complexsoft.ketnote.utils

import java.text.SimpleDateFormat
import java.util.Locale

object Constants {
    const val APP_VERSION = "2310-RC-1 Trinity"
    const val IMAGES_DATABASE = "images_db"
    const val IMAGE_TO_UPLOAD_TABLE = "image_to_upload_table"
    const val IMAGE_TO_DELETE_TABLE = "image_to_delete_table"
}

fun Long.toHumanDate(): String {
    val format = SimpleDateFormat("yyyy.dd.MM HH:mm", Locale.ENGLISH)
    return format.format(this)
}