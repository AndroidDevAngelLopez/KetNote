package com.complexsoft.ketnote.data.network

import com.complexsoft.ketnote.data.model.Note
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

interface MongoRepository {
    fun signOutWithMongoAtlas()

    //suspend fun signInWithMongoAtlas(tokenId: String)
    fun configureTheRealm()
    fun getNotes(): Flow<List<Note>>
    suspend fun createNote(currentTitle: String, currentText: String)
    suspend fun updateNote(noteId: ObjectId, newTitle: String, newText: String)
    fun getNoteById(noteId: BsonObjectId): Note?
    suspend fun deleteAllNotes()
}