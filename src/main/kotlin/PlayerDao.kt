package org.example.botfarm

interface PlayerDao {
    suspend fun allPlayers(): List<Player>
    suspend fun player(playerId: Long): Player?
    suspend fun addNewPlayer(username: String, nickname: String): Player?
    suspend fun deletePlayer(playerId: Long): Boolean
}