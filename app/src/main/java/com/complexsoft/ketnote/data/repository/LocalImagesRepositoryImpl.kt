package com.complexsoft.ketnote.data.repository

import com.complexsoft.ketnote.data.local.datasource.LocalImagesDataSource
import com.complexsoft.ketnote.data.local.entity.ImageToUpload
import javax.inject.Inject

class LocalImagesRepositoryImpl @Inject constructor(
    private val localImagesDataSource: LocalImagesDataSource
) : LocalImagesRepository {
    override suspend fun getAllImages(): List<ImageToUpload> {
        return localImagesDataSource.getAllImages()
    }

    override suspend fun addImageToUpload(imageToUpload: ImageToUpload) {
        localImagesDataSource.addImageToUpload(imageToUpload = imageToUpload)
    }

    override suspend fun cleanupImage(imageId: Int) {
        localImagesDataSource.cleanupImage(imageId = imageId)
    }
}