package org.icpclive.reactionsbot.db

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.runBlocking
import org.icpclive.reactionsbot.db.documents.*

object MongoClient {
    private val host = System.getenv("MONGO_HOST") ?: "localhost"
    private val port = System.getenv("MONGO_PORT") ?: "27017"
    private val user = System.getenv("MONGO_USER") ?: "root"
    private val password = System.getenv("MONGO_PASSWORD") ?: "secret"
    private val databaseName = System.getenv("MONGO_DATABASE") ?: "test"

    private val connectionUrl = "mongodb://$user:$password@$host:$port"
    private val client = MongoClient.create(connectionUrl)
    private val database = client.getDatabase(databaseName)

    var reactionVideosCollection: MongoCollection<ReactionVideo>
    var contestInfoItemsCollection: MongoCollection<ContestInfoItem>
    var runInfoItemsCollection: MongoCollection<RunInfoItem>

    var reactionsCollection: MongoCollection<Reaction>
    var votesCollection: MongoCollection<Vote>

    init {
        runBlocking {
            reactionVideosCollection = database.getCollection("ReactionVideos")
            contestInfoItemsCollection = database.getCollection("ContestInfoItems")
            runInfoItemsCollection = database.getCollection("RunInfoItems")

            reactionsCollection = database.getCollection("Reactions")
            votesCollection = database.getCollection("Votes")
        }
    }

    fun close() {
        client.close()
    }
}
