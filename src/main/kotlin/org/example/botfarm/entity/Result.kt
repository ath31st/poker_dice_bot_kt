package org.example.botfarm.entity

import java.time.LocalDateTime
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * Represents a game result.
 *
 * @property resultId   The unique identifier of the game result.
 * @property player     The player associated with the result.
 * @property roundTime  The date and time when the result was recorded.
 * @property groupId    The identifier of the group to which the result belongs.
 */
class Result(
    val resultId: Long,
    val player: Player,
    val roundTime: LocalDateTime,
    val groupId: Long,
)

/**
 * Represents the database table for storing game results.
 */
object Results : Table("results") {
    val resultId = long("result_id").autoIncrement()
    val playerId = long("player_id") references Players.id
    val roundTime = datetime("round_time")
    val groupId = long("group_id")

    override val primaryKey = PrimaryKey(resultId)
}
