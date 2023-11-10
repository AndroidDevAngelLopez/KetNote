package com.complexsoft.ketnote.data.local.datasource

import com.complexsoft.ketnote.data.local.entity.ImageToDelete
import com.complexsoft.ketnote.data.local.entity.ImageToDeleteDAO
import com.complexsoft.ketnote.data.local.entity.ImageToUpload
import com.complexsoft.ketnote.data.local.entity.ImageToUploadDAO
import javax.inject.Inject

class LocalImagesDataSourceImpl @Inject constructor(
    private val imageToUploadDAO: ImageToUploadDAO, private val imageToDeleteDAO: ImageToDeleteDAO
) : LocalImagesDataSource {
    override suspend fun getAllUploadImages(): List<ImageToUpload> {
        return imageToUploadDAO.getAllUploadImages()
    }

    override suspend fun addImageToUpload(imageToUpload: ImageToUpload) {
        imageToUploadDAO.addImageToUpload(imageToUpload = imageToUpload)
    }

    override suspend fun cleanupImageToUpload(remotePath:String) {
        imageToUploadDAO.cleanupImageToUpload(remotePath = remotePath)
    }

    override suspend fun getAllDeleteImages(): List<ImageToDelete> {
        return imageToDeleteDAO.getAllDeleteImages()
    }

    override suspend fun addImageToDelete(imageToDelete: ImageToDelete) {
        imageToDeleteDAO.addImageToDelete(imageToDelete = imageToDelete)
    }

    override suspend fun cleanupImageToDelete(ownerId : String) {
        imageToDeleteDAO.cleanupImageToDelete(ownerId = ownerId)
    }
}