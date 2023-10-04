package org.example.botfarm.service

import java.time.LocalDateTime
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

/**
 * The `ResultService` class provides database operations related to game results.
 * It implements the [ResultDao] interface for database access.
 */
class ResultService : ResultDao {

    /**
     * Converts a database [ResultRow] into a [Result] object.
     *
     * @param row The [ResultRow] representing a game result.
     * @return A [Result] object populated with data from the [ResultRow].
     */
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

    /**
     * Adds a new game result to the database.
     *
     * @param groupId The unique ID of the game group.
     * @param winnerId The unique ID of the player who won the game.
     */
    override suspend fun addNewResult(groupId: Long, winnerId: Long): Unit = dbQuery {
        Results.insert {
            it[Results.groupId] = groupId
            it[playerId] = winnerId
            it[roundTime] = LocalDateTime.now()
        }
    }

    /**
     * Retrieves game results for a specific game group within a specified time range.
     *
     * @param groupId The unique ID of the game group.
     * @param roundTimeStart The start time of the time range.
     * @param roundTimeEnd The end time of the time range.
     * @return A list of [Result] objects representing game results within the specified time range.
     */
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
