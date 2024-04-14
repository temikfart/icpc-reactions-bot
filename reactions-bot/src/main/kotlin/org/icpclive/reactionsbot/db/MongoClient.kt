package org.icpclive.reactionsbot.db

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.RunInfo

object MongoClient {
    private val host = System.getenv("MONGO_HOST") ?: "localhost"
    private val port = System.getenv("MONGO_PORT") ?: "27017"
    private val user = System.getenv("MONGO_USER") ?: "root"
    private val password = System.getenv("MONGO_PASSWORD") ?: "secret"
    private val databaseName = System.getenv("MONGO_DATABASE") ?: "test"

    private val connectionUrl = "mongodb://$user:$password@$host:$port"
    private val client = MongoClient.create(connectionUrl)
    private val database = client.getDatabase(databaseName)

    private var reactionsCollection: MongoCollection<Reaction>
    private var votesCollection: MongoCollection<Vote>

    init {
        runBlocking {
            reactionsCollection = database.getCollection("Reactions")
            votesCollection = database.getCollection("Votes")
        }
    }

    fun addReaction(
        reactionVideoId: ObjectId,
        telegramFileId: String = "",
        rating: Int = 0,
        voteCount: Int = 0
    ): ObjectId? = runBlocking {
        reactionsCollection.withDocumentClass<Reaction>()
            .find(eq(Reaction::telegramFileId.name, telegramFileId))
            .firstOrNull()?.id
            ?: insertReaction(Reaction(null, reactionVideoId, telegramFileId, rating, voteCount))
    }

    private suspend fun insertReaction(reaction: Reaction): ObjectId? =
        reactionsCollection.insertOne(reaction)
            .insertedId?.asObjectId()?.value

    fun addVote(
        reactionId: ObjectId,
        chatId: Long,
        vote: Int? = null
    ): ObjectId? = runBlocking {
        votesCollection.withDocumentClass<Vote>()
            .find(eq(Vote::chatId.name, chatId))
            .firstOrNull()?.id
            ?: insertVote(Vote(null, reactionId, chatId, vote))
    }

    private suspend fun insertVote(vote: Vote): ObjectId? =
        votesCollection.insertOne(vote)
            .insertedId?.asObjectId()?.value

    fun close() {
        client.close()
    }
}
