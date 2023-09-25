package org.example.botfarm.entity

import org.jetbrains.exposed.sql.Table

class Player {
    var playerId: Long = 0
    var username: String = ""
    var nickname: String = ""
    var discriminator: String = ""
    var results: List<Result> = mutableListOf()
}


object Players : Table("players") {
    val id = long("player_id").autoIncrement()
    val username = varchar("username", 500)
    val nickname = varchar("nickname", 500)
    val discriminator = varchar("discriminator", 500)

    override val primaryKey = PrimaryKey(id)
}