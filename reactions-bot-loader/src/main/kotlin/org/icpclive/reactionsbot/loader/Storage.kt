package org.icpclive.reactionsbot.loader

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class Storage {
    private val connection = Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC")

    init {
        transaction(connection) {
            SchemaUtils.create(Reactions)
        }
    }

    fun addReactions(teamId: String, problemId: String, runId: String, isOk: Boolean, fileName: String): Reaction =
        transaction(connection) {
            return@transaction Reaction.wrapRow(
                Reactions.selectAll().where { Reactions.fileName eq fileName }.singleOrNull<ResultRow>()
                    ?: Reactions.insert {
                        it[Reactions.teamId] = teamId
                        it[Reactions.problemId] = problemId
                        it[Reactions.runId] = runId
                        it[Reactions.isOk] = isOk
                        it[Reactions.fileName] = fileName
                    }.resultedValues!!.first()
            )
        }
}
