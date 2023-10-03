package org.example.botfarm.dao

import org.example.botfarm.entity.Player

/**
 * Interface for accessing player data.
 */
interface PlayerDao {

    /**
     * Checks the existence of a player with the specified identifier.
     *
     * @param playerId The player's identifier.
     * @return `true` if a player with the specified identifier exists, otherwise `false`.
     */
    suspend fun existsPlayer(playerId: Long): Boolean

    /**
     * Checks the existence of a player by identifier and first name.
     *
     * @param playerId   The player's identifier.
     * @param firstName  The player's first name.
     * @return `true` if a player with the specified identifier and name exists, otherwise `false`.
     */
    suspend fun existsByIdAndFirstName(playerId: Long, firstName: String): Boolean

    /**
     * Updates a player's first name by their identifier.
     *
     * @param playerId  The player's identifier.
     * @param firstName The new first name for the player.
     * @return The number of updated records in the database (usually 1 if the update is successful).
     */
    suspend fun updateFirstNameById(playerId: Long, firstName: String): Int

    /**
     * Retrieves player information by their identifier.
     *
     * @param playerId The player's identifier.
     * @return A [Player] object representing player information, or `null` if the player is not found.
     */
    suspend fun player(playerId: Long): Player?

    /**
     * Retrieves the player's name by their identifier.
     *
     * @param playerId The player's identifier.
     * @return The player's first name as a string, or username if the first name is empty.
     */
    suspend fun playerName(playerId: Long): String

    /**
     * Adds a new player with the specified data.
     *
     * @param playerId  The identifier for the new player.
     * @param username  The username of the new player.
     * @param firstName The first name of the new player.
     * @param lastName  The last name of the new player.
     * @return A [Player] object representing the new player, or `null` if the addition fails.
     */
    suspend fun addNewPlayer(
        playerId: Long,
        username: String,
        firstName: String,
        lastName: String,
    ): Player?

    /**
     * Deletes a player by their identifier.
     *
     * @param playerId The identifier of the player to be deleted.
     * @return `true` if the player is successfully deleted, otherwise `false`.
     */
    suspend fun deletePlayer(playerId: Long): Boolean
}

