package org.example.botfarm.dao

import org.example.botfarm.entity.Player

interface PlayerDao {
    suspend fun existsPlayer(playerId: Long): Boolean
    suspend fun player(playerId: Long): Player?
    suspend fun addNewPlayer(
        playerId: Long,
        username: String,
        firstName: String,
        lastName: String
    ): Player?

    suspend fun deletePlayer(playerId: Long): Boolean
}