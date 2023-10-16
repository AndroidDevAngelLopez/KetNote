package com.complexsoft.ketnote.data.local.datasource

import com.complexsoft.ketnote.data.local.entity.ImageToDelete
import com.complexsoft.ketnote.data.local.entity.ImageToUpload

interface LocalImagesDataSource {
    suspend fun getAllUploadImages(): List<ImageToUpload>
    suspend fun addImageToUpload(imageToUpload: ImageToUpload)
    suspend fun cleanupImageToUpload(imageId: Int)
    suspend fun getAllDeleteImages(): List<ImageToDelete>
    suspend fun addImageToDelete(imageToDelete: ImageToDelete)
    suspend fun cleanupImageToDelete(imageId: Int)

}