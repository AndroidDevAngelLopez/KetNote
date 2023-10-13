package com.complexsoft.ketnote.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.complexsoft.ketnote.data.local.entity.ImageToUpload
import com.complexsoft.ketnote.data.local.entity.ImageToUploadDAO

@Database(
    entities = [ImageToUpload::class], version = 1, exportSchema = false
)
abstract class ImagesDatabase : RoomDatabase() {
    abstract fun imageToUploadDao(): ImageToUploadDAO
}