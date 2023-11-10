package com.complexsoft.ketnote.domain.usecases

import android.net.Uri
import android.util.Log
import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.data.repository.LocalImagesRepository
import com.complexsoft.ketnote.data.repository.MongoDB
import com.complexsoft.ketnote.ui.screen.utils.NoteJobUiState
import com.complexsoft.ketnote.ui.screen.utils.NoteUiState
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class HandleNotesUseCase @Inject constructor(
    private val localImagesRepository: LocalImagesRepository
) {

    val noteUiState = MutableStateFlow(NoteUiState())
    val isNoteJobDone = MutableStateFlow(NoteJobUiState())
    fun updateNoteUiStateFlow(
        title: String, text: String, imageUri: String
    ) = noteUiState.update {
        it.copy(
            title = title, text = text, image = imageUri
        )
    }

    fun updateIsNoteJobDone(value: Boolean) = isNoteJobDone.update {
        it.copy(value = value)
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

    suspend fun updateNote(id: ObjectId, title: String, text: String, image: String) =
        MongoDB.updateNote(id, title, text, image)

    suspend fun insertNote(title: String, text: String, image: String) =
        MongoDB.createNote(title, text, image)

}