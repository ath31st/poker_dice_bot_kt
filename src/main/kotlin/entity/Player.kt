package org.example.botfarm.entity

import org.jetbrains.exposed.sql.Table

class Player {
    var playerId: Long = 0
    var username: String = ""
    var firstName: String = ""
    var lastName: String = ""
    var results: List<Result> = mutableListOf()
}

object Players : Table("players") {
    val id = long("player_id")
    val username = varchar("username", 500)
    val firstName = varchar("first_name", 500)
    val lastName = varchar("last_name", 500)

    override val primaryKey = PrimaryKey(id)
}