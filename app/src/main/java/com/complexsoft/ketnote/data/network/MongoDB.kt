package com.complexsoft.ketnote.data.network

import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.data.network.MongoDBAPP.app
import com.complexsoft.ketnote.utils.Constants.APP_ID
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId


object MongoDBAPP {
    var app: App = App.create(APP_ID)
}

object MongoDB : MongoRepository {

    lateinit var realm: Realm

    init {
        configureTheRealm()
    }

    override fun configureTheRealm() {
        if (app.currentUser != null) {
            val config = SyncConfiguration.Builder(app.currentUser!!, setOf(Note::class))
                .initialSubscriptions { sub ->
                    add(query = sub.query<Note>(query = "owner_id == $0", app.currentUser!!.id))
                }.log(LogLevel.ALL).build()
            realm = Realm.open(config)
        }
    }

    override suspend fun deleteNoteById(noteId: ObjectId) {
        realm.write {
            val note: Note = this.query<Note>("_id == $0", noteId).find().first()
            delete(note)
        }
    }

    override fun getNotes(): Flow<List<Note>> {
        return realm.query<Note>().sort("date", Sort.DESCENDING).asFlow().map { it.list }
    }

    override fun searchNotesByTitle(title: String): Flow<List<Note>> {
        return realm.query<Note>("title CONTAINS[c] $0", title).sort("date", Sort.DESCENDING)
            .asFlow().map { it.list }
    }

    override suspend fun createNote(currentTitle: String, currentText: String) {
        realm.writeBlocking {
            copyToRealm(Note().apply {
                title = currentTitle
                date = System.currentTimeMillis()
                text = currentText
                owner_id = app.currentUser?.id ?: ""
            })
        }
    }

    override suspend fun updateNote(noteId: ObjectId, newTitle: String, newText: String) {
        realm.write {
            val note: Note? = this.query<Note>("_id == $0", noteId).first().find()
            note?.title = newTitle
            note?.text = newText
        }
    }

    override suspend fun deleteAllNotes() {
        realm.write {
            val notes: RealmResults<Note>? =
                app.currentUser?.let { this.query<Note>(query = "owner_id == $0", it.id).find() }
            if (notes != null) {
                delete(notes)
            }
        }
    }

    override fun getNoteById(noteId: BsonObjectId): Note? {
        return realm.query<Note>("_id == $0", noteId).first().find()
    }
}