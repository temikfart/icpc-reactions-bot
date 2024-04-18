package org.icpclive.reactionsbot

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.long
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.icpclive.reactionsbot.db.Storage
import java.io.File

class ReactionsBot(
    val telegramToken: String,
    val botSystemChat: Long,
) {
    private val bot = bot {
        logLevel = LogLevel.Error
        token = telegramToken
        setupDispatch()
    }
    private val sendAdditionalInfo = true

    private fun reactionRatingButtons(reactionId: ObjectId): InlineKeyboardMarkup {
        return InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("Like \uD83D\uDC4D", "vote:$reactionId:like"),
            InlineKeyboardButton.CallbackData("Dislike \uD83D\uDC4E", "vote:$reactionId:dislike"),
        )
    }

    private fun Bot.Builder.setupDispatch() {
        dispatch {
            text {
                sendNextReactionVideo(message.chat)
            }

            callbackQuery("vote:") {
                val query = this.callbackQuery.data.split(":")
                if (query.size == 3) {
                    val reactionId = ObjectId(query[1])
                    val chatId = this.callbackQuery.message?.chat?.id ?: 0L
                    val delta = when (query[2]) {
                        "like" -> +1
                        "dislike" -> -1
                        else -> 0
                    }
                    Storage.storeReactionVote(reactionId, chatId, delta)
                    sendNextReactionVideo(callbackQuery.message!!.chat)
                }
            }
        }
    }

    private fun sendNextReactionVideo(chat: Chat) {
        val reaction = Storage.getReactionForVote(chat.id)
        if (reaction == null) {
            bot.sendMessage(ChatId.fromId(chat.id), "No more reaction videos to vote for")
            return
        }

        val reactionVideo = Storage.getReactionVideo(reaction.reactionVideoId)
        if (reactionVideo == null) {
            bot.sendMessage(
                ChatId.fromId(botSystemChat),
                "[${chat.id}] No such reaction video with ID ${reaction.reactionVideoId}"
            )
            return
        }

        val runInfoItem = Storage.getRunInfoItem(reactionVideo.runInfoItemId)
        if (runInfoItem == null) {
            bot.sendMessage(
                ChatId.fromId(botSystemChat),
                "[${chat.id}] No such run info item with ID ${reactionVideo.runInfoItemId}"
            )
            return
        }

        val contestInfo = Storage.getContestInfo(reactionVideo.contestId)?.contestInfo
        if (contestInfo == null) {
            bot.sendMessage(
                ChatId.fromId(botSystemChat),
                "[${chat.id}] No such contest with ID ${reactionVideo.contestId}"
            )
            return
        }

        var caption: String? = null
        val runInfo = runInfoItem.runInfo
        if (sendAdditionalInfo) {
            contestInfo.teams[runInfo.teamId]?.let { team ->
                contestInfo.problems[runInfo.problemId]?.let { problem ->
                    caption = "${team.fullName}, problem ${problem.displayName}"
                }
            }
        }

        val videoFile = File(reactionVideo.fileName)
        bot.sendVideo(
            ChatId.fromId(chat.id),
            TelegramFile.ByFile(videoFile),
            caption = caption,
            replyMarkup = reactionRatingButtons(reaction.id!!)
        )
    }

    fun run(scope: CoroutineScope) {
        scope.launch { bot.startPolling() }
    }
}

class ReactionsBotCommand : CliktCommand() {
    private val token by option(help = "Telegram bot token")
        .required()
    private val chat by option("--chat", help = "System chat id for bot management")
        .long()
        .default(-1)

    override fun run() {
        runBlocking {
            ReactionsBot(
                telegramToken = token,
                botSystemChat = chat,
            ).run(this)
        }
    }
}

fun main(args: Array<String>) {
    ReactionsBotCommand().main(args)
}
