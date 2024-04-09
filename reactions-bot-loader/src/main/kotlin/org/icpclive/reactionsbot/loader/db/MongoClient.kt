package org.icpclive.reactionsbot.loader.db

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId

object MongoClient {
    private val host = System.getenv("MONGO_HOST") ?: "localhost"
    private val port = System.getenv("MONGO_PORT") ?: "27017"
    private val user = System.getenv("MONGO_USER") ?: "root"
    private val password = System.getenv("MONGO_PASSWORD") ?: "secret"
    private val databaseName = System.getenv("MONGO_DATABASE") ?: "test"

    private val connectionUrl = "mongodb://$user:$password@$host:$port"
    private val client = MongoClient.create(connectionUrl)
    private val database = client.getDatabase(databaseName)

    private var reactionVideoCollection: MongoCollection<ReactionVideo>

    init {
        runBlocking {
            database.createCollection("ReactionVideos")
            reactionVideoCollection = database.getCollection("ReactionVideos")
        }
    }

    fun addReactionVideo(
        teamId: String,
        problemId: String,
        runId: String,
        isOk: Boolean,
        fileName: String
    ): ObjectId? = runBlocking {
        reactionVideoCollection.withDocumentClass<ReactionVideo>()
            .find(eq(ReactionVideo::fileName.name, fileName))
            .firstOrNull()?.id
            ?: insertReactionVideo(teamId, problemId, runId, isOk, fileName)
    }

    private suspend fun insertReactionVideo(
        teamId: String,
        problemId: String,
        runId: String,
        isOk: Boolean,
        fileName: String
    ): ObjectId? = reactionVideoCollection
        .insertOne(ReactionVideo(null, teamId, problemId, runId, isOk, fileName))
        .insertedId
        ?.asObjectId()
        ?.value

    fun close() {
        client.close()
    }
}
