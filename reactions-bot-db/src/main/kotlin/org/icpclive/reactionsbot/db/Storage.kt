package org.icpclive.reactionsbot.db

import org.bson.types.ObjectId
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.RunInfo
import org.icpclive.reactionsbot.db.repositories.*

object Storage {
    fun addReaction(
        reactionVideoId: ObjectId,
        telegramFileId: String = "",
        rating: Int = 0,
        voteCount: Int = 0
    ): ObjectId? = ReactionRepository.add(reactionVideoId, telegramFileId, rating, voteCount)

    fun addVote(
        reactionId: ObjectId,
        chatId: Long,
        vote: Int? = null
    ): ObjectId? = VoteRepository.add(reactionId, chatId, vote)

    fun addReactionVideo(
        contestId: String,
        teamId: String,
        problemId: String,
        runId: String,
        isOk: Boolean,
        fileName: String
    ): ObjectId? = ReactionVideoRepository.add(contestId, teamId, problemId, runId, isOk, fileName)

    fun addOrReplaceContestInfoItem(contestId: String, contestInfo: ContestInfo): ObjectId? =
        ContestInfoItemRepository.addOrReplace(contestId, contestInfo)

    fun addRunInfoItem(contestId: String, runInfo: RunInfo): ObjectId? =
        RunInfoItemRepository.add(contestId, runInfo)
}
