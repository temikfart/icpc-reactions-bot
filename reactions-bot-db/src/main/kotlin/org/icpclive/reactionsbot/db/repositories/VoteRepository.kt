package org.icpclive.reactionsbot.db.repositories

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.FindFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.icpclive.reactionsbot.db.MongoClient
import org.icpclive.reactionsbot.db.documents.Vote

object VoteRepository {
    fun add(
        reactionId: ObjectId,
        chatId: Long,
        vote: Int?
    ): ObjectId? = runBlocking {
        MongoClient.votesCollection.withDocumentClass<Vote>()
            .find(eq(Vote::chatId.name, chatId))
            .firstOrNull()?.id
            ?: insert(Vote(null, reactionId, chatId, vote))
    }

    private suspend fun insert(vote: Vote): ObjectId? =
        MongoClient.votesCollection.insertOne(vote)
            .insertedId?.asObjectId()?.value

    fun getAllByFilter(filter: Bson): FindFlow<Vote> =
        MongoClient.votesCollection.withDocumentClass<Vote>()
            .find(filter)
}
