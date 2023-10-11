package com.complexsoft.ketnote.data.repository

import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.ui.screen.utils.NotesState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

interface MongoRepository {
    fun configureTheRealm()
    suspend fun deleteNoteById(noteId: ObjectId)
    fun getNotes(): Flow<NotesState<List<Note>>>
    fun searchNotesByTitle(title: String): Flow<NotesState<List<Note>>>
    suspend fun createNote(currentTitle: String, currentText: String,image :String)
    suspend fun updateNote(noteId: ObjectId, newTitle: String, newText: String,image:String)
    fun getNoteById(noteId: BsonObjectId): Note?
    suspend fun deleteAllNotes()
}