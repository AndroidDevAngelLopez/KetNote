package com.complexsoft.ketnote.domain.usecases

import com.complexsoft.ketnote.data.repository.MongoDB.createNote
import com.complexsoft.ketnote.ui.screen.utils.NoteUiState
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(
    private val uploadPhotoToFirebaseUseCase: UploadPhotoToFirebaseUseCase,
) {
    suspend operator fun invoke(uploadTask: StorageReference, currentNote: NoteUiState) {
        if (uploadTask.path != "/") {
            uploadPhotoToFirebaseUseCase(uploadTask, currentNote.image.toString()) {
                CoroutineScope(Dispatchers.IO).launch {
                    createNote(
                        currentTitle = currentNote.title, currentText = currentNote.text, image = it
                    )
                }
            }
        } else {
            createNote(
                currentNote.title, currentNote.text, currentNote.image.toString()
            )
        }
    }
}