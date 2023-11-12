package com.complexsoft.ketnote.domain.usecases

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.StorageReference

class UploadPhotoToFirebaseUseCase {
    operator fun invoke(
        uploadTask: StorageReference, image: String, onUriDownloadReceived: (String) -> Unit
    ) {
        uploadTask.putFile(Uri.parse(image)).addOnFailureListener {
            Log.d("failure in upload image", it.message.toString())
        }.addOnSuccessListener {
            uploadTask.downloadUrl.addOnSuccessListener {
                onUriDownloadReceived(it.toString())
            }.addOnFailureListener {
                Log.d("EXCEPTOIN ON RETURNING URL", "from firebase $it")
            }
        }
    }
}