package com.complexsoft.ketnote.data.repository

import com.complexsoft.ketnote.data.local.datasource.LocalImagesDataSource
import com.complexsoft.ketnote.data.local.entity.ImageToDelete
import com.complexsoft.ketnote.data.local.entity.ImageToUpload
import javax.inject.Inject

class LocalImagesRepositoryImpl @Inject constructor(
    private val localImagesDataSource: LocalImagesDataSource
) : LocalImagesRepository {
    override suspend fun getAllUploadImages(): List<ImageToUpload> {
        return localImagesDataSource.getAllUploadImages()
    }

    override suspend fun addImageToUpload(imageToUpload: ImageToUpload) {
        localImagesDataSource.addImageToUpload(imageToUpload = imageToUpload)
    }

    override suspend fun cleanupImageToUpload(remotePath: String) {
        localImagesDataSource.cleanupImageToUpload(remotePath = remotePath)
    }

    override suspend fun getAllDeleteImages(): List<ImageToDelete> {
        return localImagesDataSource.getAllDeleteImages()
    }

    override suspend fun addImageToDelete(imageToDelete: ImageToDelete) {
        localImagesDataSource.addImageToDelete(imageToDelete = imageToDelete)
    }

    override suspend fun cleanupImageToDelete(ownerId : String) {
        localImagesDataSource.cleanupImageToDelete(ownerId = ownerId)
    }
}