package org.icpclive.reactionsbot.db.repositories

import com.mongodb.client.model.Filters
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.icpclive.reactionsbot.db.MongoClient
import org.icpclive.reactionsbot.db.documents.Reaction

object ReactionRepository {
    fun add(
        reactionVideoId: ObjectId,
        telegramFileId: String,
        rating: Int,
        voteCount: Int
    ): ObjectId? = runBlocking {
        MongoClient.reactionsCollection.withDocumentClass<Reaction>()
            .find(Filters.eq(Reaction::telegramFileId.name, telegramFileId))
            .firstOrNull()?.id
            ?: insert(Reaction(null, reactionVideoId, telegramFileId, rating, voteCount))
    }

    private suspend fun insert(reaction: Reaction): ObjectId? =
        MongoClient.reactionsCollection.insertOne(reaction)
            .insertedId?.asObjectId()?.value
}
