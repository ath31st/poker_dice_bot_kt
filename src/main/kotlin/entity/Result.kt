package org.example.botfarm.entity

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

class Result(
    val resultId: Long,
    val player: Player,
    val roundTime: LocalDateTime,
    val groupId: Long,
)

object Results : Table("results") {
    val resultId = long("result_id").autoIncrement()
    val playerId = long("player_id") references Players.id
    val roundTime = datetime("round_time")
    val groupId = long("group_id")

    override val primaryKey = PrimaryKey(resultId)
}