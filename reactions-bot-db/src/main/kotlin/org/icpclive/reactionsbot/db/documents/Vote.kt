package org.icpclive.reactionsbot.db.documents

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Vote(
    @BsonId val id: ObjectId? = null,
    val reactionId: ObjectId,
    val chatId: Long,
    val vote: Int?,
)
