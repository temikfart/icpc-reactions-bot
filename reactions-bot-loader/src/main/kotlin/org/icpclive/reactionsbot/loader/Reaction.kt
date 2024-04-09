package org.icpclive.reactionsbot.loader

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Reactions : IntIdTable() {
    val teamId = Reactions.varchar("teamId", 100)
    val problemId = Reactions.varchar("problemId", 100)
    val runId = Reactions.varchar("runID", 100)
    val isOk = Reactions.bool("isOk")
    val fileName = Reactions.varchar("fileName", 200)
}

class Reaction(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Reaction>(Reactions)

    var teamId by Reactions.teamId
    var problemId by Reactions.problemId
    var runId by Reactions.runId
    var isOk by Reactions.isOk
    var fileName by Reactions.fileName
}
