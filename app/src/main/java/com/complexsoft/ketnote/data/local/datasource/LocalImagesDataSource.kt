package com.complexsoft.ketnote.data.local.datasource

import com.complexsoft.ketnote.data.local.entity.ImageToUpload

interface LocalImagesDataSource {
    suspend fun getAllImages(): List<ImageToUpload>
    suspend fun addImageToUpload(imageToUpload: ImageToUpload)
    suspend fun cleanupImage(imageId: Int)
}