package com.complexsoft.ketnote.data.repository

import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.data.model.NotificationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

interface MongoRepository {
    fun configureTheRealm()
    suspend fun deleteNoteById(noteId: ObjectId)
    fun getNotes(): Flow<List<Note>>
    fun getNotifications() : Flow<List<NotificationItem>>
    fun searchNotesByTitle(title: String): Flow<List<Note>>
    suspend fun createNotification(
       header: String,
       body :String,
       flag : Boolean
    )

    suspend fun deleteAllNotifications()
    suspend fun createNote(currentTitle: String, currentText: String, image: String)
    suspend fun updateNote(noteId: ObjectId, newTitle: String, newText: String, image: String)
    fun getNoteById(noteId: BsonObjectId): Note?
    suspend fun deleteAllNotes()
}