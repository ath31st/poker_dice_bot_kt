package org.example.botfarm

import org.jetbrains.exposed.sql.Table

data class Player(
    val playerId: Long,
    val username: String,
    val nickname: String,
    val discriminator: String
)

object Players : Table("players") {
    val id = long("player_id").autoIncrement()
    val username = varchar("username", 500)
    val nickname = varchar("nickname", 500)
    val discriminator = varchar("discriminator", 500)
    override val primaryKey = PrimaryKey(id)
}