package org.example.botfarm.service

import com.github.kotlintelegrambot.entities.Message
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.botfarm.entity.PlayerInRound
import org.example.botfarm.entity.PokerRound
import org.example.botfarm.util.DiceUtil
import org.example.botfarm.util.MessageEnum
import org.example.botfarm.util.RandomPhrase
import org.example.botfarm.util.StringUtil
import java.util.concurrent.ConcurrentMap

class RoundService(
    private val playerService: PlayerService,
    private val resultService: ResultService,
    private val rounds: ConcurrentMap<Long, PokerRound>
) {
    fun startNewRound(groupId: Long, playerInitiator: Long, playerName: String): String {
        if (rounds.containsKey(groupId)) {
            return MessageEnum.TABLE_BUSY.value
        }
        val players = HashMap<Long, PlayerInRound>()

        val instant = Clock.System.now()
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        rounds[groupId] = PokerRound(
            playerInitiator,
            isEnded = false,
            groupId,
            players,
            localDateTime,
            actionCounter = 0
        )

        return MessageEnum.START_ROUND.value.format(playerName)
    }

    suspend fun rollDices(message: Message): String {
        var result = ""
        val groupId: Long = message.chat.id
        val playerId: Long = message.from!!.id
        if (checkRoundAvailable(groupId, playerId)) {
            val pr = rounds[groupId]
            pr!!.actionCounter = pr.actionCounter + 2

            val playerName = message.from!!.firstName.isBlank().let { message.from!!.username!! }

            if (!playerService.existsPlayer(playerId)) {
                playerService.addNewPlayer(
                    playerId,
                    username = message.from!!.username ?: "",
                    firstName = message.from!!.firstName,
                    lastName = message.from!!.lastName ?: ""
                )
            } else {
                playerService.checkAndUpdateFirstName(playerId, playerName)
            }

            val rollDices: IntArray = DiceUtil.roll5d6()

            val pir = playerService.createPiR()
            pir.name = playerName
            pir.dices = rollDices
            pir.isRoll = false
            pr.players[playerId] = pir
            pr.actionCounter -= 1

            result = RandomPhrase.getRollDicesPhrase()
                .format(playerName, StringUtil.resultWithBrackets(rollDices))
        }
        return result
    }

    private fun checkRoundAvailable(groupId: Long, playerId: Long): Boolean {
        val result: Boolean = if (!rounds.containsKey(groupId)) {
            false
        } else {
            val pr = rounds[groupId]
            !pr!!.isEnded && !pr.players.containsKey(playerId)
        }
        return result
    }
}