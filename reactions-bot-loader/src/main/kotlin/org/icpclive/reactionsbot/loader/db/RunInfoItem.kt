package org.icpclive.reactionsbot.loader.db

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.icpclive.cds.api.RunInfo

data class RunInfoItem(
    @BsonId val id: ObjectId? = null,
    val contestId: String,
    val runInfo: RunInfo,
)
