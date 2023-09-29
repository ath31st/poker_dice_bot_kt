package org.example.botfarm.service

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.botfarm.DatabaseFactory.dbQuery
import org.example.botfarm.dao.ResultDao
import org.example.botfarm.entity.Player
import org.example.botfarm.entity.Players
import org.example.botfarm.entity.Result
import org.example.botfarm.entity.Results
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.LocalDateTime

class ResultService : ResultDao {

    private fun resultRowToResult(row: ResultRow): Result {
        val winner = Players
            .select { Players.id eq row[Results.playerId] }
            .single()
            .let { playerRow ->
                val player = Player()
                player.playerId = playerRow[Players.id]
                player.username = playerRow[Players.username]
                player.firstName = playerRow[Players.firstName]
                player.lastName = playerRow[Players.lastName]
                player
            }

        return Result(
            resultId = row[Results.resultId],
            player = winner,
            roundTime = row[Results.roundTime],
            groupId = row[Results.groupId],
        )
    }

    override suspend fun addNewResult(groupId: Long, winnerId: Long): Unit = dbQuery {
        val instant = Clock.System.now()
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        Results.insert {
            it[Results.groupId] = groupId
            it[playerId] = winnerId
            it[roundTime] = localDateTime
        }
    }

    override suspend fun findByGroupIdAndRoundTimeBetween(
        groupId: Long,
        roundTimeStart: LocalDateTime,
        roundTimeEnd: LocalDateTime,
    ): List<Result> = dbQuery {
        Results
            .select {
                (Results.groupId eq groupId) and (
                    Results.roundTime.between(
                        roundTimeStart,
                        roundTimeEnd,
                    )
                    )
            }
            .map(::resultRowToResult)
            .toList()
    }
}
