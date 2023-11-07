package com.complexsoft.ketnote.domain.usecases

import android.net.Uri
import android.util.Log
import com.complexsoft.ketnote.data.local.entity.ImageToDelete
import com.complexsoft.ketnote.data.local.entity.ImageToUpload
import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.data.repository.LocalImagesRepository
import com.complexsoft.ketnote.data.repository.MongoDB
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class HandleNotesUseCase @Inject constructor(
    private val localImagesRepository: LocalImagesRepository
) {

    suspend fun addImageToUpload(
        remoteImagePath: String, imageUri: String, ownerId: String
    ) {
        val imageToUpload = ImageToUpload(
            remoteImagePath = remoteImagePath, imageUri = imageUri, ownerId = ownerId
        )
        localImagesRepository.addImageToUpload(imageToUpload)
    }

    suspend fun uploadLocalImages(
        onUriDownloadReceived: (String, String) -> Unit,
    ) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        var counter = 0
        localImagesRepository.getAllUploadImages().asFlow().collectLatest {
            uploadPhotoToFirebase(storageRef.child(it.remoteImagePath), it.imageUri) { uri ->
                onUriDownloadReceived(uri, it.ownerId)
            }
        }
    }


    suspend fun addImageToDelete(
        remoteImagePath: String, ownerId: String
    ) {
        val imageToDelete = ImageToDelete(
            remoteImagePath = remoteImagePath, ownerId = ownerId
        )
        localImagesRepository.addImageToDelete(imageToDelete = imageToDelete)
    }

    fun uploadPhotoToFirebase(
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

    fun deletePhotoFromFirebase(imageToDelete: StorageReference, onImageDeleted: () -> Unit) {
        imageToDelete.delete().addOnSuccessListener {
            onImageDeleted()
        }
    }

    fun searchNotesByTitle(title: String) = MongoDB.searchNotesByTitle(title)
    suspend fun deleteNoteById(noteId: ObjectId) = MongoDB.deleteNoteById(noteId)
    fun getAllNotes() = MongoDB.getNotes()
    fun getNoteById(noteId: ObjectId): Note? = MongoDB.getNoteById(noteId)
    suspend fun deleteAllNotes(storage: FirebaseStorage) {
        getAllNotes().collectLatest {
            for (note in it) {
                if (note.images.isNotEmpty()) {
                    val toDeleteRef = storage.getReferenceFromUrl(note.images)
                    deletePhotoFromFirebase(toDeleteRef) {
                        Log.d("Firebase on all notes deleted : ", "image successfully deleted!")
                    }
                }
            }
            MongoDB.deleteAllNotes()
        }
    }

    suspend fun updateNote(id: ObjectId, title: String, text: String, image: String) =
        MongoDB.updateNote(id, title, text, image)

    suspend fun insertNote(title: String, text: String, image: String) =
        MongoDB.createNote(title, text, image)

}