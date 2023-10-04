package org.example.botfarm.scheduler

import java.time.LocalDateTime
import java.util.concurrent.ConcurrentMap
import org.example.botfarm.entity.PokerRound

/**
 * The `PokerDiceScheduler` object manages the scheduling and finalization of poker rounds.
 * It provides a method to determine which rounds should be finalized based on their start time
 * and a predefined duration.
 */
object PokerDiceScheduler {
    // The duration (in minutes) after which a poker round should be finalized if not already done.
    private const val DURATION = 5L

    /**
     * Finalizes poker rounds that have exceeded the specified duration since their start time.
     *
     * @param rounds A concurrent map containing active poker rounds, where the key is the group ID
     *               and the value is a [PokerRound] object representing the round.
     *
     * @return A list of pairs, where each pair contains the group ID of a finalized round and a list
     *         of player names who have automatically passed their turn during that round.
     */
    fun finalizeRounds(
        rounds: ConcurrentMap<Long, PokerRound>,
    ): List<Pair<Long, List<String>>> {
        val result: MutableList<Pair<Long, List<String>>> = mutableListOf()
        val currentTime = LocalDateTime.now()
        val iterator = rounds.entries.iterator()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            val pokerRound = entry.value

            if (pokerRound.startRound.plusMinutes(DURATION)
                    .isBefore(currentTime)
            ) {
                val groupId = entry.key
                val autoPassPlayers = pokerRound.players
                    .filter { it.value.isReroll && it.value.isPass }
                    .map { it.value.name }
                    .toList()

                result += Pair(groupId, autoPassPlayers)
            }
        }
        return result
    }
}
