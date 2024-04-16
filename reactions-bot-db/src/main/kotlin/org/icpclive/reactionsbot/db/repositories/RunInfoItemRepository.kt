package org.icpclive.reactionsbot.db.repositories

import com.mongodb.client.model.Filters.eq
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.icpclive.cds.api.RunInfo
import org.icpclive.reactionsbot.db.MongoClient
import org.icpclive.reactionsbot.db.documents.RunInfoItem

object RunInfoItemRepository {
    fun add(contestId: String, runInfo: RunInfo): ObjectId? = runBlocking {
        MongoClient.runInfoItemsCollection.withDocumentClass<RunInfoItem>()
            .find(eq("${RunInfoItem::runInfo.name}.${RunInfo::id.name}", runInfo.id))
            .firstOrNull()?.id
            ?: insert(RunInfoItem(null, contestId, runInfo))

    }

    private suspend fun insert(runInfoItem: RunInfoItem): ObjectId? =
        MongoClient.runInfoItemsCollection.insertOne(runInfoItem)
            .insertedId?.asObjectId()?.value
}
