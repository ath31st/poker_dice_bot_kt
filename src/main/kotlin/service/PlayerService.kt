package org.example.botfarm.service

import org.example.botfarm.DatabaseFactory.dbQuery
import org.example.botfarm.dao.PlayerDao
import org.example.botfarm.entity.Player
import org.example.botfarm.entity.Players
import org.example.botfarm.entity.Result
import org.example.botfarm.entity.Results
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class PlayerService : PlayerDao {

    private fun resultRowToPlayer(row: ResultRow): Player {
        val player = Player()
        player.playerId = row[Players.id]
        player.nickname = row[Players.nickname]
        player.username = row[Players.username]
        player.discriminator = row[Players.discriminator]
        player.results = Results.select { Results.playerId eq row[Players.id] }
            .map {
                Result(
                    resultId = it[Results.resultId],
                    player = player,
                    roundTime = it[Results.roundTime],
                    idChannel = it[Results.idChannel]
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

    override suspend fun allPlayers(): List<Player> = dbQuery {
        Players
            .selectAll()
            .map(::resultRowToPlayer)
    }


    override suspend fun addNewPlayer(username: String, nickname: String): Player? = dbQuery {
        val insertStatement = Players.insert {
            it[Players.username] = username
            it[Players.nickname] = nickname
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToPlayer)
    }

    override suspend fun deletePlayer(playerId: Long): Boolean = dbQuery {
        Players
            .deleteWhere { id eq playerId } > 0
    }
}