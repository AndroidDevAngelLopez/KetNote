package com.complexsoft.ketnote.data.local.datasource

import com.complexsoft.ketnote.data.local.entity.ImageToDelete
import com.complexsoft.ketnote.data.local.entity.ImageToUpload

interface LocalImagesDataSource {
    suspend fun getAllUploadImages(): List<ImageToUpload>
    suspend fun addImageToUpload(imageToUpload: ImageToUpload)
    suspend fun cleanupImageToUpload(remotePath: String)
    suspend fun getAllDeleteImages(): List<ImageToDelete>
    suspend fun addImageToDelete(imageToDelete: ImageToDelete)
    suspend fun cleanupImageToDelete(ownerId: String)

}