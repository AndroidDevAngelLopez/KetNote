package com.complexsoft.ketnote.data.repository

import com.complexsoft.ketnote.data.local.entity.ImageToUpload

interface LocalImagesRepository {
    suspend fun getAllImages(): List<ImageToUpload>
    suspend fun addImageToUpload(imageToUpload: ImageToUpload)
    suspend fun cleanupImage(imageId: Int)
}