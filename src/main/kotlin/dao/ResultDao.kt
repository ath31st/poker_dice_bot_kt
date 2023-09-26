package org.example.botfarm.dao

import org.example.botfarm.entity.Result
import java.time.LocalDateTime


interface ResultDao {
    suspend fun addNewResult(groupId: Long, winnerId: Long)
    suspend fun findByGroupIdAndRoundTimeBetween(
        groupId: Long, roundTimeStart: LocalDateTime, roundTimeEnd: LocalDateTime
    ): List<Result>
}