package org.example.botfarm.dao

import java.time.LocalDateTime
import org.example.botfarm.entity.Result

interface ResultDao {
    suspend fun addNewResult(groupId: Long, winnerId: Long)
    suspend fun findByGroupIdAndRoundTimeBetween(
        groupId: Long,
        roundTimeStart: LocalDateTime,
        roundTimeEnd: LocalDateTime,
    ): List<Result>
}
