package com.complexsoft.ketnote.domain.usecases

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.data.repository.MongoDB
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class HandleNotesUseCase {

    fun uploadPhotoToFirebase(
        uploadTask: StorageReference, image: Uri, onUriDownloadReceived: (String) -> Unit
    ) {
        uploadTask.putFile(image).addOnFailureListener {
            Log.d("failure in upload image", it.message.toString())
        }.addOnSuccessListener { taskSnapshot ->
            uploadTask.downloadUrl.addOnSuccessListener {
                onUriDownloadReceived(it.toString())
            }
        }
    }

    fun generateJpegImage(note: Note, activity: DialogFragment): Bitmap {
        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawPaint(paint)
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.textSize = 30f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(note.title, 8f * note.title.length.toFloat(), 35f, paint)
        paint.textSize = 16f
        canvas.drawText(note.text, 3.6f * note.text.length.toFloat(), 400f, paint)
        Log.d("SIZE VALUE OF NOTE TEXT", (3.6f * note.text.length.toFloat()).toString())
        CoroutineScope(Dispatchers.IO).launch {
            canvas.drawBitmap(
                Glide.with(activity).asBitmap().load(note.images).submit(300, 300).get(),
                width / 5f,
                60f,
                paint
            )
            withContext(Dispatchers.IO) {
                val file = File(activity.context?.externalCacheDir?.absolutePath, "image.jpg")
                try {
                    val outputStream = FileOutputStream(file)
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG, 100, outputStream
                    )
                    outputStream.flush()
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return bitmap
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

    fun openPhotoShareDialog(activity: DialogFragment) =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            {
                if (result.resultCode == Activity.RESULT_OK) {

                }
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