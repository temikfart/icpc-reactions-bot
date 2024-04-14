package org.icpclive.reactionsbot.db.repositories

import com.mongodb.client.model.Filters
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.icpclive.cds.api.ContestInfo
import org.icpclive.reactionsbot.db.MongoClient
import org.icpclive.reactionsbot.db.documents.ContestInfoItem

object ContestInfoItemRepository {
    fun addOrReplace(contestId: String, contestInfo: ContestInfo): ObjectId? = runBlocking {
        val contestInfoItem = ContestInfoItem(null, contestId, contestInfo)
        val updateResult = MongoClient.contestInfoItemsCollection
            .replaceOne(Filters.eq(ContestInfoItem::contestId.name, contestId), contestInfoItem)

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
}
