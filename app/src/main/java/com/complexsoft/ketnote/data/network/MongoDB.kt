package com.complexsoft.ketnote.data.network

import com.complexsoft.ketnote.data.model.Note
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
import kotlinx.coroutines.runBlocking
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

object MongoDB : MongoRepository {
    val app = App.Companion.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm

    init {
        configureTheRealm()
    }

    override fun configureTheRealm() {
        val config = user?.let {
            SyncConfiguration.Builder(
                it, setOf(Note::class)
            ).initialSubscriptions { sub ->
                add(query = sub.query<Note>(query = "owner_id == $0", user.id))
            }.log(LogLevel.ALL).build()
        }
        realm = config?.let { Realm.open(it) }!!

    }

    override fun signOutWithMongoAtlas() {
        runBlocking {
            val user = app.currentUser
            user?.logOut()
        }
    }

    override fun getNotes(): Flow<List<Note>> {
        return realm.query<Note>().sort("date", Sort.DESCENDING).asFlow().map { it.list }
    }

    override suspend fun createNote(currentTitle: String, currentText: String) {
        realm.writeBlocking {
            copyToRealm(Note().apply {
                title = currentTitle
                date = System.currentTimeMillis()
                text = currentText
                owner_id = user?.id ?: ""
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
            val notes: RealmResults<Note> =
                this.query<Note>(query = "owner_id == $0", user?.id).find()
            delete(notes)
        }
    }

    override fun getNoteById(noteId: BsonObjectId): Note? {
        val note: Note? = realm.query<Note>("_id == $0", noteId).first().find()
        return note
    }
}