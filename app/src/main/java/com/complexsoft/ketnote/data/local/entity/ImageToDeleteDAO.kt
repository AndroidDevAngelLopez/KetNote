package com.complexsoft.ketnote.data.local.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageToDeleteDAO {

    @Query("SELECT * FROM image_to_delete_table ORDER BY id ASC")
    suspend fun getAllDeleteImages(): List<ImageToDelete>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImageToDelete(imageToDelete: ImageToDelete)

    @Query("DELETE FROM image_to_delete_table WHERE id=:imageId")
    suspend fun cleanupImageToDelete(imageId: Int)

}