package com.complexsoft.ketnote.data.local.datasource

import com.complexsoft.ketnote.data.local.entity.ImageToUpload
import com.complexsoft.ketnote.data.local.entity.ImageToUploadDAO
import javax.inject.Inject

class LocalImagesDataSourceImpl @Inject constructor(
    private val imageToUploadDAO: ImageToUploadDAO
) : LocalImagesDataSource {
    override suspend fun getAllImages(): List<ImageToUpload> {
        return imageToUploadDAO.getAllImages()
    }

    override suspend fun addImageToUpload(imageToUpload: ImageToUpload) {
        imageToUploadDAO.addImageToUpload(imageToUpload = imageToUpload)
    }

    override suspend fun cleanupImage(imageId: Int) {
        imageToUploadDAO.cleanupImage(imageId = imageId)
    }
}