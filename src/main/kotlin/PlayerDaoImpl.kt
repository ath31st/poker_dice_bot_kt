package org.example.botfarm

import org.example.botfarm.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class PlayerDaoImpl : PlayerDao {
    private fun resultRowToPlayer(row: ResultRow) = Player(
        playerId = row[Players.id],
        nickname = row[Players.nickname],
        username = row[Players.username],
        discriminator = row[Players.discriminator],
    )

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
            .deleteWhere { Players.id eq playerId } > 0
    }
}