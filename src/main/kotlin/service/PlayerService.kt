package org.example.botfarm.service

import org.example.botfarm.DatabaseFactory.dbQuery
import org.example.botfarm.dao.PlayerDao
import org.example.botfarm.entity.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class PlayerService : PlayerDao {

    private fun resultRowToPlayer(row: ResultRow): Player {
        val player = Player()
        player.playerId = row[Players.id]
        player.username = row[Players.username]
        player.firstName = row[Players.firstName]
        player.lastName = row[Players.lastName]
        player.results = Results.select { Results.playerId eq row[Players.id] }
            .map {
                Result(
                    resultId = it[Results.resultId],
                    player = player,
                    roundTime = it[Results.roundTime],
                    groupId = it[Results.groupId]
                )
            }
        return player
    }


    override suspend fun player(playerId: Long): Player? = dbQuery {
        Players
            .select { Players.id eq playerId }
            .map(::resultRowToPlayer)
            .singleOrNull()
    }

    override suspend fun existsPlayer(playerId: Long): Boolean = dbQuery {
        Players
            .select { Players.id eq playerId }
            .count().toInt() == 1
    }

    override suspend fun addNewPlayer(
        playerId: Long,
        username: String,
        firstName: String,
        lastName: String
    ): Player? = dbQuery {
        val insertStatement = Players.insert {
            it[id] = playerId
            it[Players.username] = username
            it[Players.firstName] = firstName
            it[Players.lastName] = lastName
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToPlayer)
    }

    override suspend fun deletePlayer(playerId: Long): Boolean = dbQuery {
        Players
            .deleteWhere { id eq playerId } > 0
    }

    fun createPiR(): PlayerInRound {
        return PlayerInRound("", isRoll = true, isReroll = true, isPass = true, IntArray(5), 0)
    }
}