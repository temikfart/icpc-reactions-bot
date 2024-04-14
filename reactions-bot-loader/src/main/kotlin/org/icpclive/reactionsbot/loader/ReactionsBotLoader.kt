package org.icpclive.reactionsbot.loader

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.bson.types.ObjectId
import org.icpclive.cds.InfoUpdate
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.adapters.contestState
import org.icpclive.cds.adapters.processHiddenProblems
import org.icpclive.cds.adapters.processHiddenTeamsAndGroups
import org.icpclive.cds.adapters.removeFrozenSubmissions
import org.icpclive.cds.api.MediaType
import org.icpclive.cds.api.RunInfo
import org.icpclive.cds.api.RunResult
import org.icpclive.cds.cli.CdsCommandLineOptions
import org.icpclive.reactionsbot.db.MongoClient
import org.icpclive.util.getLogger
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories

class ReactionsBotLoader(
    val cdsOptions: CdsCommandLineOptions,
    val disableCdsLoader: Boolean,
    val loaderThreads: Int,
    val videoPathPrefix: String,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val reactionsProcessingPool = Dispatchers.IO.limitedParallelism(loaderThreads)
    private val cds = cdsOptions.toFlow(getLogger(ReactionsBotLoader::class))
        .contestState()
        .removeFrozenSubmissions()
        .processHiddenTeamsAndGroups()
        .processHiddenProblems()
    private val alreadyProcessedReactionIds = TreeSet<ObjectId>()
    private val contestId = "46th"

    private fun processReaction(scope: CoroutineScope, run: RunInfo, reactionUrl: String) {
        val reactionVideoId = MongoClient.addReactionVideo(
            contestId,
            run.teamId.value,
            run.problemId.value,
            run.id.value,
            (run.result as? RunResult.ICPC)?.verdict?.isAccepted == true,
            reactionUrl
        )
        if (reactionVideoId != null && reactionVideoId !in alreadyProcessedReactionIds) {
            alreadyProcessedReactionIds.add(reactionVideoId)
            scope.launch(reactionsProcessingPool) {
                Path.of("converted").createDirectories()
                val outputFileName = "converted/${reactionVideoId}.mp4"
                try {
                    convertVideo(videoPathPrefix + reactionUrl, outputFileName)
                } catch (ignore: FfmpegException) {
                    println(ignore)
                }
            }
        }
    }

    fun run(scope: CoroutineScope) {
        val loader = if (disableCdsLoader) emptyFlow() else cds.shareIn(scope, SharingStarted.Eagerly, Int.MAX_VALUE)
        val runUpdates = loader.filterIsInstance<RunUpdate>().map { it.newInfo }
        val infoUpdates = loader.filterIsInstance<InfoUpdate>().map { it.newInfo }

        val contest = runBlocking { infoUpdates.first() }
        scope.launch {
            println("starting runUpdates processing ...")
            println("runUpdates processing stated for ${contest.currentContestTime}")
            runUpdates.collect { runUpdate ->
                MongoClient.addRunInfoItem(contestId, runUpdate)
                runUpdate.reactionVideos.forEach {
                    if (it is MediaType.M2tsVideo) {
                        processReaction(scope, runUpdate, it.url)
                    }
                }
            }
        }
        scope.launch {
            println("starting infoUpdates processing ...")
            println("infoUpdates processing stated for ${contest.currentContestTime}")
            infoUpdates.collect { infoUpdate ->
                MongoClient.addOrReplaceContestInfoItem(contestId, infoUpdate)
            }
        }
    }
}

class ReactionsBotLoaderCommand : CliktCommand() {
    private val cdsSettings by CdsCommandLineOptions()
    private val disableCds by option(help = "Enable loading events from cds")
        .flag()
    private val threads by option("--threads", "-t", help = "Count of video converter and loader threads")
        .int()
        .default(Runtime.getRuntime().availableProcessors() - 1)
    private val video by option( help = "Prefix for video url")
        .default("")

    override fun run() {
        runBlocking {
            ReactionsBotLoader(
                cdsOptions = cdsSettings,
                disableCdsLoader = disableCds,
                loaderThreads = threads,
                videoPathPrefix = video,
            ).run(this)
        }
    }
}

fun main(args: Array<String>) {
    ReactionsBotLoaderCommand().main(args)
}
