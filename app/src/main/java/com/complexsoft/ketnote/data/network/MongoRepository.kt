package com.complexsoft.ketnote.data.network

import com.complexsoft.ketnote.data.model.Note
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

interface MongoRepository {
    fun configureTheRealm()
    suspend fun deleteNoteById(noteId: ObjectId)
    fun getNotes(): Flow<List<Note>>
    fun searchNotesByTitle(title: String): Flow<List<Note>>
    suspend fun createNote(currentTitle: String, currentText: String,image :String)
    suspend fun updateNote(noteId: ObjectId, newTitle: String, newText: String,image:String)
    fun getNoteById(noteId: BsonObjectId): Note?
    suspend fun deleteAllNotes()
}