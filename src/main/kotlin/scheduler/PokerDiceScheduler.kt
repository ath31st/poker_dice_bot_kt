package org.example.botfarm.scheduler

import kotlinx.datetime.toJavaLocalDateTime
import org.example.botfarm.entity.PokerRound
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentMap

object PokerDiceScheduler {
    private const val DURATION = 1L

    fun finalizeRounds(
        rounds: ConcurrentMap<Long, PokerRound>
    ): List<Pair<Long, List<String>>> {
        val result: MutableList<Pair<Long, List<String>>> = mutableListOf()
        val currentTime = LocalDateTime.now()
        val iterator = rounds.entries.iterator()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            val pokerRound = entry.value

            if (pokerRound.startRound.toJavaLocalDateTime().plusMinutes(DURATION)
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