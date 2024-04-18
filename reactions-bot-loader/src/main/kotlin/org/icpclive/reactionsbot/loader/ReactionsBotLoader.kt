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
import org.icpclive.reactionsbot.db.Storage
import org.icpclive.util.getLogger
import java.io.File
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
    private val alreadyProcessedRunIds = TreeSet<ObjectId>()
    private val contestId = "46th"

    fun run(scope: CoroutineScope) {
        val loader = when (disableCdsLoader) {
            true -> emptyFlow()
            false -> cds.shareIn(scope, SharingStarted.Eagerly, Int.MAX_VALUE)
        }
        val runUpdates = loader.filterIsInstance<RunUpdate>().map { it.newInfo }
        val infoUpdates = loader.filterIsInstance<InfoUpdate>().map { it.newInfo }

        val contest = runBlocking { infoUpdates.first() }
        scope.launch {
            println("starting runUpdates processing ...")
            println("runUpdates processing stated for ${contest.currentContestTime}")
            runUpdates.collect { runUpdate ->
                val acceptedRun = (runUpdate.result as? RunResult.ICPC)?.verdict?.isAccepted == true
                when (acceptedRun) {
                    true -> processRun(scope, runUpdate, acceptedRun)
                    false -> println("Run is not accepted: ${runUpdate.id}. Skipping")
                }
            }
        }
        scope.launch {
            println("starting infoUpdates processing ...")
            println("infoUpdates processing stated for ${contest.currentContestTime}")
            infoUpdates.collect { infoUpdate ->
                val teams = infoUpdate.teams.entries.associateBy({ it.key.value }, { it.value })
                val problems = infoUpdate.problems.entries.associateBy({ it.key.value }, { it.value })
                Storage.addOrReplaceContestInfoItem(contestId, teams, problems)
            }
        }
    }

    private fun processRun(scope: CoroutineScope, runUpdate: RunInfo, acceptedRun: Boolean) {
        val runInfoItemId = Storage.addRunInfoItem(contestId, runUpdate, acceptedRun)
        if (runInfoItemId != null) {
            runUpdate.reactionVideos.forEach {
                if (it is MediaType.M2tsVideo) {
                    processReactionVideo(scope, runInfoItemId, it.url)
                }
            }
        } else {
            println("Error adding runInfo")
        }
    }

    private fun processReactionVideo(scope: CoroutineScope, runInfoItemId: ObjectId, reactionUrl: String) {
        if (runInfoItemId !in alreadyProcessedRunIds) {
            alreadyProcessedRunIds.add(runInfoItemId)
            scope.launch(reactionsProcessingPool) {
                try {
                    Path.of("converted").createDirectories()
                    val outputFileName = "converted/${runInfoItemId}.mp4"
                    val reactionVideoId = Storage.addReactionVideo(
                        contestId,
                        runInfoItemId,
                        File(outputFileName).absolutePath
                    )
                    Storage.addReaction(reactionVideoId!!) // TODO: add null check
                    convertVideo(videoPathPrefix + reactionUrl, outputFileName)
                } catch (ignore: FfmpegException) {
                    println(ignore)
                }
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
