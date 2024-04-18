package org.icpclive.reactionsbot.db.repositories

import com.mongodb.client.model.Filters.eq
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.icpclive.reactionsbot.db.MongoClient
import org.icpclive.reactionsbot.db.documents.ReactionVideo

object ReactionVideoRepository {
    fun add(contestId: String, runId: ObjectId, fileName: String): ObjectId? = runBlocking {
        MongoClient.reactionVideosCollection.withDocumentClass<ReactionVideo>()
            .find(eq(ReactionVideo::fileName.name, fileName))
            .firstOrNull()?.id
            ?: insert(ReactionVideo(null, contestId, runId, fileName))
    }

    private suspend fun insert(reactionVideo: ReactionVideo): ObjectId? =
        MongoClient.reactionVideosCollection.insertOne(reactionVideo)
            .insertedId?.asObjectId()?.value

    fun getById(id: ObjectId): ReactionVideo? = runBlocking {
        MongoClient.reactionVideosCollection.withDocumentClass<ReactionVideo>()
            .find(eq("_id", id))
            .firstOrNull()
    }
}
