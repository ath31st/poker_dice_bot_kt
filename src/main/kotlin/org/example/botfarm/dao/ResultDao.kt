package org.example.botfarm.dao

import java.time.LocalDateTime
import org.example.botfarm.entity.Result

/**
 * Interface for accessing result data.
 */
interface ResultDao {

    /**
     * Adds a new result record indicating the winner in a group.
     *
     * @param groupId  The identifier of the group for which the result is recorded.
     * @param winnerId The identifier of the winning player.
     */
    suspend fun addNewResult(groupId: Long, winnerId: Long)

    /**
     * Finds results by group identifier and within a specified time range.
     *
     * @param groupId         The identifier of the group to search for results.
     * @param roundTimeStart  The start time of period to search within.
     * @param roundTimeEnd    The end time of period to search within.
     * @return A list of [Result] objects representing results found within the specified time range.
     */
    suspend fun findByGroupIdAndRoundTimeBetween(
        groupId: Long,
        roundTimeStart: LocalDateTime,
        roundTimeEnd: LocalDateTime,
    ): List<Result>
}

