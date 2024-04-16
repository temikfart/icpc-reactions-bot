package org.icpclive.reactionsbot.db

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.ne
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Sorts.orderBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.RunInfo
import org.icpclive.reactionsbot.db.documents.ContestInfoItem
import org.icpclive.reactionsbot.db.documents.Reaction
import org.icpclive.reactionsbot.db.documents.ReactionVideo
import org.icpclive.reactionsbot.db.documents.Vote
import org.icpclive.reactionsbot.db.repositories.*

object Storage {
    fun addReaction(
        reactionVideoId: ObjectId,
        telegramFileId: String = "",
        rating: Int = 0,
        voteCount: Int = 0
    ): ObjectId? = ReactionRepository.add(reactionVideoId, telegramFileId, rating, voteCount)

    fun addVote(reactionId: ObjectId, chatId: Long, vote: Int? = null): ObjectId? =
        VoteRepository.add(reactionId, chatId, vote)

    fun addReactionVideo(contestId: String, runId: ObjectId, fileName: String): ObjectId? =
        ReactionVideoRepository.add(contestId, runId, fileName)

    fun addOrReplaceContestInfoItem(contestId: String, contestInfo: ContestInfo): ObjectId? =
        ContestInfoItemRepository.addOrReplace(contestId, contestInfo)

    fun addRunInfoItem(contestId: String, runInfo: RunInfo, accepted: Boolean): ObjectId? =
        RunInfoItemRepository.add(contestId, runInfo, accepted)

    fun getReactionForVote(chatId: Long): Reaction? = runBlocking {
        val reactionsFlow = ReactionRepository
            .getAllByFilter(ne(Reaction::telegramFileId.name, null))
            .sort(orderBy(Sorts.ascending(Reaction::voteCount.name)))

        val votedReactionIds = VoteRepository
            .getAllByFilter(eq(Vote::chatId.name, chatId))
            .map { it.id }
            .filterNotNull()
            .toList()

        reactionsFlow.firstOrNull { it.id !in votedReactionIds }
    }

    fun getReactionVideo(reactionVideoId: ObjectId): ReactionVideo? =
        ReactionVideoRepository.getById(reactionVideoId)

    fun getContestInfo(contestId: String): ContestInfoItem? =
        ContestInfoItemRepository.getById(contestId)

    fun storeReactionVote(reactionId: ObjectId, chatId: Long, delta: Int) {
        val reaction = ReactionRepository.getById(reactionId)
        if (reaction != null) {
            val updatedReaction = reaction.copy(
                voteCount = reaction.voteCount + 1,
                rating = reaction.rating + delta
            )
            ReactionRepository.replace(reactionId, updatedReaction)
        }
        addVote(reactionId, chatId, delta)
    }
}
