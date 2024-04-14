package org.icpclive.reactionsbot.db

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.RunInfo
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

    private var reactionVideosCollection: MongoCollection<ReactionVideo>
    private var contestInfoItemsCollection: MongoCollection<ContestInfoItem>
    private var runInfoItemsCollection: MongoCollection<RunInfoItem>

    private var reactionsCollection: MongoCollection<Reaction>
    private var votesCollection: MongoCollection<Vote>

    init {
        runBlocking {
            reactionVideosCollection = database.getCollection("ReactionVideos")
            contestInfoItemsCollection = database.getCollection("ContestInfoItems")
            runInfoItemsCollection = database.getCollection("RunInfoItems")

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

    fun addReactionVideo(
        contestId: String,
        teamId: String,
        problemId: String,
        runId: String,
        isOk: Boolean,
        fileName: String
    ): ObjectId? = runBlocking {
        reactionVideosCollection.withDocumentClass<ReactionVideo>()
            .find(eq(ReactionVideo::fileName.name, fileName))
            .firstOrNull()?.id
            ?: insertReactionVideo(ReactionVideo(null, contestId, teamId, problemId, runId, isOk, fileName))
    }

    private suspend fun insertReactionVideo(reactionVideo: ReactionVideo): ObjectId? =
        reactionVideosCollection.insertOne(reactionVideo)
            .insertedId?.asObjectId()?.value

    fun addOrReplaceContestInfoItem(contestId: String, contestInfo: ContestInfo): ObjectId? = runBlocking {
        val contestInfoItem = ContestInfoItem(null, contestId, contestInfo)
        val updateResult = contestInfoItemsCollection
            .replaceOne(eq(ContestInfoItem::contestId.name, contestId), contestInfoItem)

        val notReplaced = updateResult.matchedCount == 0L
        if (notReplaced) {
            insertContestInfoItem(contestInfoItem)
        } else {
            updateResult.upsertedId?.asObjectId()?.value
        }
    }

    private suspend fun insertContestInfoItem(contestInfoItem: ContestInfoItem): ObjectId? =
        contestInfoItemsCollection.insertOne(contestInfoItem)
            .insertedId?.asObjectId()?.value

    fun addRunInfoItem(contestId: String, runInfo: RunInfo): ObjectId? = runBlocking {
        runInfoItemsCollection.withDocumentClass<RunInfoItem>()
            .find(eq("${RunInfoItem::runInfo.name}.${RunInfo::id.name}", runInfo.id))
            .firstOrNull()?.id
            ?: insertRunInfoItem(RunInfoItem(null, contestId, runInfo))

    }

    private suspend fun insertRunInfoItem(runInfoItem: RunInfoItem): ObjectId? =
        runInfoItemsCollection.insertOne(runInfoItem)
            .insertedId?.asObjectId()?.value

    fun close() {
        client.close()
    }
}
