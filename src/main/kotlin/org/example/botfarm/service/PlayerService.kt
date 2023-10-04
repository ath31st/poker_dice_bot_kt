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

/**
 * The `PlayerService` class provides database operations and utilities related to player management.
 * It implements the [PlayerDao] interface for database access.
 */
class PlayerService : PlayerDao {

    /**
     * Converts a database [ResultRow] into a [Player] object.
     *
     * @param row The [ResultRow] representing a player's data.
     * @return A [Player] object populated with data from the [ResultRow].
     */
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

    /**
     * Retrieves a player from the database by their unique player ID.
     *
     * @param playerId The unique ID of the player to retrieve.
     * @return The [Player] object if found, or null if not found.
     */
    override suspend fun player(playerId: Long): Player? = dbQuery {
        Players
            .select { Players.id eq playerId }
            .map(::resultRowToPlayer)
            .singleOrNull()
    }

    /**
     * Retrieves the name of a player by their unique player ID.
     *
     * @param playerId The unique ID of the player.
     * @return The player's name, using their first name if available, or their username if not.
     */
    override suspend fun playerName(playerId: Long): String = dbQuery {
        Players
            .select { Players.id eq playerId }
            .map { it[Players.firstName].ifEmpty { it[Players.username] } }
            .single()
    }

    /**
     * Checks if a player with the given player ID exists in the database.
     *
     * @param playerId The unique ID of the player to check.
     * @return `true` if the player exists, `false` otherwise.
     */
    override suspend fun existsPlayer(playerId: Long): Boolean = dbQuery {
        Players
            .select { Players.id eq playerId }
            .count().toInt() == 1
    }

    /**
     * Adds a new player to the database.
     *
     * @param playerId The unique ID of the player to add.
     * @param username The player's username.
     * @param firstName The player's first name.
     * @param lastName The player's last name.
     * @return The newly added [Player] object, or null if the insertion fails.
     */
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

    /**
     * Deletes a player from the database by their unique player ID.
     *
     * @param playerId The unique ID of the player to delete.
     * @return `true` if the player is successfully deleted, `false` otherwise.
     */
    override suspend fun deletePlayer(playerId: Long): Boolean = dbQuery {
        Players
            .deleteWhere { id eq playerId } > 0
    }

    /**
     * Checks if a player with the given player ID and first name exists in the database.
     *
     * @param playerId The unique ID of the player to check.
     * @param firstName The first name to check.
     * @return `true` if a matching player exists, `false` otherwise.
     */
    override suspend fun existsByIdAndFirstName(playerId: Long, firstName: String): Boolean =
        dbQuery {
            Players
                .select {
                    (Players.id eq playerId) andIfNotNull (Players.firstName eq firstName)
                }.count().toInt() == 1
        }

    /**
     * Updates the first name of a player in the database by their unique player ID.
     *
     * @param playerId The unique ID of the player to update.
     * @param firstName The new first name for the player.
     * @return The number of affected rows (1 if successful, 0 otherwise).
     */
    override suspend fun updateFirstNameById(playerId: Long, firstName: String): Int =
        dbQuery {
            Players.update({ Players.id eq playerId }) { it[Players.firstName] = firstName }
        }

    /**
     * Checks if a player's first name matches the provided first name and updates it if they do not match.
     *
     * @param playerId The unique ID of the player to check and update.
     * @param firstName The new first name to set for the player.
     */
    suspend fun checkAndUpdateFirstName(playerId: Long, firstName: String) {
        if (!existsByIdAndFirstName(playerId, firstName)) {
            updateFirstNameById(playerId, firstName)
        }
    }

    /**
     * Creates a new [PlayerInRound] object with default values.
     *
     * @return A new [PlayerInRound] object with default values.
     */
    fun createPiR(): PlayerInRound {
        return PlayerInRound("", isRoll = true, isReroll = true, isPass = true, IntArray(5), 0)
    }
}
