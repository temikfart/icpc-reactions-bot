package org.icpclive.reactionsbot.db

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Reaction(
    @BsonId val id: ObjectId? = null,
    val reactionVideoId: ObjectId,
    val telegramFileId: String,
    val rating: Int,
    val voteCount: Int,
)
