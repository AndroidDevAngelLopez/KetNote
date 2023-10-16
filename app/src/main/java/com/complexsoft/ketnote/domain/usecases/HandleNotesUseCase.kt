package com.complexsoft.ketnote.domain.usecases

import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.complexsoft.ketnote.data.local.entity.ImageToUpload
import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.data.repository.LocalImagesRepository
import com.complexsoft.ketnote.data.repository.MongoDB
import com.complexsoft.ketnote.ui.screen.utils.NotesState
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.collectLatest
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class HandleNotesUseCase @Inject constructor(
    private val localImagesRepository: LocalImagesRepository
) {
    suspend fun addImageToLocalDatabase(
        remoteImagePath: String, imageUri: String, ownerId: String
    ) {
        val imageToUpload = ImageToUpload(
            remoteImagePath = remoteImagePath, imageUri = imageUri, ownerId = ownerId
        )
        localImagesRepository.addImageToUpload(imageToUpload)
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

    fun openPhotoPicker(activity: DialogFragment, onImagesFetched: (images: List<Uri>) -> Unit) =
        activity.registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(5)
        ) { images ->
            if (images != null) {
                onImagesFetched(images)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    fun searchNotesByTitle(title: String) = MongoDB.searchNotesByTitle(title)
    suspend fun deleteNoteById(noteId: ObjectId) = MongoDB.deleteNoteById(noteId)
    fun getAllNotes() = MongoDB.getNotes()
    fun getNoteById(noteId: ObjectId): Note? = MongoDB.getNoteById(noteId)
    suspend fun deleteAllNotes(storage: FirebaseStorage) {
        getAllNotes().collectLatest {
            if (it is NotesState.Success) {
                for (note in it.data) {
                    if (note.images.isNotEmpty()) {
                        val toDeleteRef = storage.getReferenceFromUrl(note.images)
                        deletePhotoFromFirebase(toDeleteRef) {
                            Log.d("Firebase on all notes deleted : ", "image successfully deleted!")
                        }
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