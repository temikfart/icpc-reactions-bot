package org.icpclive.reactionsbot.loader.db

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class ReactionVideo(
    @BsonId val id: ObjectId? = null,
    val teamId: String,
    val problemId: String,
    val runId: String,
    val isOk: Boolean,
    val fileName: String,
)
