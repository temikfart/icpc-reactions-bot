package org.icpclive.reactionsbot.db.repositories

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.FindFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.bson.conversions.Bson
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
            .find(eq(Reaction::telegramFileId.name, telegramFileId))
            .firstOrNull()?.id
            ?: insert(Reaction(null, reactionVideoId, telegramFileId, rating, voteCount))
    }

    private suspend fun insert(reaction: Reaction): ObjectId? =
        MongoClient.reactionsCollection.insertOne(reaction)
            .insertedId?.asObjectId()?.value

    fun getAllByFilter(filter: Bson): FindFlow<Reaction> =
        MongoClient.reactionsCollection.withDocumentClass<Reaction>()
            .find(filter)

    fun getById(id: ObjectId): Reaction? = runBlocking {
        MongoClient.reactionsCollection.withDocumentClass<Reaction>()
            .find(eq("_id", id))
            .firstOrNull()
    }

    fun replace(id: ObjectId, reaction: Reaction) = runBlocking {
        MongoClient.reactionsCollection
            .replaceOne(eq(Reaction::id.name, id), reaction)
    }
}
