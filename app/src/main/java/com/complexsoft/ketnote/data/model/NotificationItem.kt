package com.complexsoft.ketnote.data.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class NotificationItem : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var title: String = ""
    var description: String = ""
    var date: Long = 0L
    var owner_id: String = ""
    var seen: Boolean = false
}
