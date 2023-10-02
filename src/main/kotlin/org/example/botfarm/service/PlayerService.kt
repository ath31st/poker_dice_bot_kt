package org.example.botfarm.service

import org.example.botfarm.DatabaseFactory.dbQuery
import org.example.botfarm.dao.PlayerDao
import org.example.botfarm.entity.Player
import org.example.botfarm.entity.PlayerInRound
import org.example.botfarm.entity.Players
import org.example.botfarm.entity.Result
import org.example.botfarm.entity.Results
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andIfNotNull
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

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
                    groupId = it[Results.groupId],
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

    override suspend fun playerName(playerId: Long): String = dbQuery {
        Players
            .select { Players.id eq playerId }
            .map { it[Players.firstName].ifEmpty { it[Players.username] } }
            .single()
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
        lastName: String,
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

    override suspend fun existsByIdAndFirstName(playerId: Long, firstName: String): Boolean =
        dbQuery {
            Players
                .select {
                    (Players.id eq playerId) andIfNotNull (Players.firstName eq firstName)
                }.count().toInt() == 1
        }

    override suspend fun updateFirstNameById(playerId: Long, firstName: String): Int =
        dbQuery {
            Players.update({ Players.id eq playerId }) { it[Players.firstName] = firstName }
        }

    suspend fun checkAndUpdateFirstName(playerId: Long, firstName: String) {
        if (!existsByIdAndFirstName(playerId, firstName)) {
            updateFirstNameById(playerId, firstName)
        }
    }

    fun createPiR(): PlayerInRound {
        return PlayerInRound("", isRoll = true, isReroll = true, isPass = true, IntArray(5), 0)
    }
}