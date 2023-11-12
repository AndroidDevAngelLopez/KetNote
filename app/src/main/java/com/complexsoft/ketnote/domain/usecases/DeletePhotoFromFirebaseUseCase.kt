package com.complexsoft.ketnote.domain.usecases

import com.google.firebase.storage.StorageReference

class DeletePhotoFromFirebaseUseCase {
    operator fun invoke(imageToDelete: StorageReference, onImageDeleted: () -> Unit) {
        imageToDelete.delete().addOnSuccessListener {
            onImageDeleted()
        }
    }
}