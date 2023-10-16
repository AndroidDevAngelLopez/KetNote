package com.complexsoft.ketnote.data.local.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageToUploadDAO {

    @Query("SELECT * FROM image_to_upload_table ORDER BY id ASC")
    suspend fun getAllUploadImages(): List<ImageToUpload>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImageToUpload(imageToUpload: ImageToUpload)

    @Query("DELETE FROM image_to_upload_table WHERE id=:imageId")
    suspend fun cleanupImageToUpload(imageId: Int)

}