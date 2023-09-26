package org.example.botfarm.service

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.botfarm.entity.PlayerInRound
import org.example.botfarm.entity.PokerRound
import org.example.botfarm.util.MessageEnum
import java.util.concurrent.ConcurrentMap

class RoundService(
    private val playerService: PlayerService,
    private val resultService: ResultService,
    private val rounds: ConcurrentMap<Long, PokerRound>
) {
    suspend fun startNewRound(groupId: Long, playerInitiator: Long): String {
        if (rounds.containsKey(groupId)) {
            return MessageEnum.TABLE_BUSY.value
        }

        val players = HashMap<Long, PlayerInRound>()

        val instant = Clock.System.now()
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val pr = PokerRound(
            playerInitiator,
            isEnded = false,
            groupId,
            players,
            localDateTime,
            0
        )
        rounds[groupId] = pr

        return String().format(
            MessageEnum.START_ROUND.value,
            playerService.playerName(playerInitiator)
        )
    }
}