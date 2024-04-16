package org.icpclive.reactionsbot.db.documents

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class ReactionVideo(
    @BsonId val id: ObjectId? = null,
    val contestId: String,
    val runInfoItemId: ObjectId,
    val fileName: String,
)
