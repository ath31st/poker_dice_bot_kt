package org.example.botfarm.entity

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Table

class Result(
    val resultId: Long,
    val player: Player,
    val roundTime: LocalDateTime,
    val idChannel: Long,
)

object Results: Table("results") {
    val id = long("result_id").autoIncrement()
}