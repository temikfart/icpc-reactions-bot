package org.icpclive.reactionsbot.db.repositories

import com.mongodb.client.model.Filters.eq
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.icpclive.cds.api.ProblemInfo
import org.icpclive.cds.api.TeamInfo
import org.icpclive.reactionsbot.db.MongoClient
import org.icpclive.reactionsbot.db.documents.ContestInfoItem

object ContestInfoItemRepository {
    fun addOrReplace(
        contestId: String,
        teams: Map<String, TeamInfo>,
        problems: Map<String, ProblemInfo>
    ): ObjectId? = runBlocking {
        val contestInfoItem = ContestInfoItem(null, contestId, teams, problems)
        val updateResult = MongoClient.contestInfoItemsCollection
            .replaceOne(eq(ContestInfoItem::contestId.name, contestId), contestInfoItem)

        val notReplaced = updateResult.matchedCount == 0L
        if (notReplaced) {
            insert(contestInfoItem)
        } else {
            updateResult.upsertedId?.asObjectId()?.value
        }
    }

    private suspend fun insert(contestInfoItem: ContestInfoItem): ObjectId? =
        MongoClient.contestInfoItemsCollection.insertOne(contestInfoItem)
            .insertedId?.asObjectId()?.value

    fun getById(contestId: String): ContestInfoItem? = runBlocking {
        MongoClient.contestInfoItemsCollection.withDocumentClass<ContestInfoItem>()
            .find(eq(ContestInfoItem::contestId.name, contestId))
            .firstOrNull()
    }
}
