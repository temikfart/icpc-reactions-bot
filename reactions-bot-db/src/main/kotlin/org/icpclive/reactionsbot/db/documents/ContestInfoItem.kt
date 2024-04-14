package org.icpclive.reactionsbot.db.documents

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.icpclive.cds.api.ContestInfo

data class ContestInfoItem(
    @BsonId val id: ObjectId? = null,
    val contestId: String,
    val contestInfo: ContestInfo,
)
