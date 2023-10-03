package org.example.botfarm.entity

import org.jetbrains.exposed.sql.Table

/**
 * Represents a player in the game.
 *
 * @property playerId   The primary key column representing the player's unique identifier.
 * @property username   The column for storing the player's username.
 * @property firstName  The column for storing the player's first name.
 * @property lastName   The column for storing the player's last name.
 * @property results    A list of game results associated with the player.
 */
class Player {
    var playerId: Long = 0
    var username: String = ""
    var firstName: String = ""
    var lastName: String = ""
    var results: List<Result> = mutableListOf()
}

/**
 * Represents the database table for storing player data.
 */
object Players : Table("players") {
    val id = long("player_id")
    val username = varchar("username", 500)
    val firstName = varchar("first_name", 500)
    val lastName = varchar("last_name", 500)

    /**
     * Specifies the primary key constraint for the player table.
     */
    override val primaryKey = PrimaryKey(id)
}
