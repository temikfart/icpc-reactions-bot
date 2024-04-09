package org.icpclive.reactionsbot.loader

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.bson.Document

object MongoClientConnection {
    private val host = System.getenv("MONGO_HOST") ?: "localhost"
    private val port = System.getenv("MONGO_PORT") ?: "27017"
    private val user = System.getenv("MONGO_USER") ?: "root"
    private val password = System.getenv("MONGO_PASSWORD") ?: "secret"
    private val databaseName = System.getenv("MONGO_DATABASE") ?: "test"

    private val connectionUrl = "mongodb://$user:$password@$host:$port"
    private val client = MongoClient.create(connectionUrl)
    private val database = client.getDatabase(databaseName)

    fun main() {
        val collection: MongoCollection<Document> = database.getCollection("testCollection")

        runBlocking {
            val doc = collection.find().firstOrNull()
            if (doc != null) {
                println(doc)
            } else {
                println("No documents found")
            }
        }
    }

    fun close() {
        client.close()
    }
}
