package org.icpclive.reactionsbot.db.documents

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.icpclive.cds.api.*

data class ContestInfoItem(
    @BsonId val id: ObjectId? = null,
    val contestId: String,
    val teams: Map<String, TeamInfo>,
    val problems: Map<String, ProblemInfo>,
//    val contestInfo: ContestInfo, // TODO: error during deserialization, probably TeamId serialized as String, but expected class.
)
