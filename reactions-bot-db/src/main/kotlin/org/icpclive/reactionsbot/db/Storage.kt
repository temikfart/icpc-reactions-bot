package org.icpclive.reactionsbot.db

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.ne
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Sorts.orderBy
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.RunInfo
import org.icpclive.reactionsbot.db.documents.*
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
            .getAll()
            .sort(orderBy(Sorts.ascending(Reaction::voteCount.name)))
        println("Found ${reactionsFlow.count()} reactions")

        val votedReactionIds = VoteRepository
            .getAllByFilter(eq(Vote::chatId.name, chatId))
            .map { it.id }
            .filterNotNull()
            .toList()
        println("Found ${votedReactionIds.size} voted reactions")

        val reaction = reactionsFlow.firstOrNull { it.id !in votedReactionIds }
        println("Reaction for vote: $reaction")
        reaction
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

    fun getRunInfoItem(runInfoItemId: ObjectId): RunInfoItem? =
        RunInfoItemRepository.getById(runInfoItemId)
}
