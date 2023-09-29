package org.example.botfarm.service

import com.github.kotlintelegrambot.entities.Message
import org.example.botfarm.entity.PlayerInRound
import org.example.botfarm.entity.PokerRound
import org.example.botfarm.entity.Result
import org.example.botfarm.entity.RoundResult
import org.example.botfarm.util.Command
import org.example.botfarm.util.DiceUtil
import org.example.botfarm.util.StringUtil
import java.time.LocalDateTime
import java.util.Map.Entry.comparingByValue
import java.util.concurrent.ConcurrentMap
import java.util.regex.Pattern

class RoundService(
    private val playerService: PlayerService,
    private val resultService: ResultService,
    private val scoreService: ScoreService,
    private val rounds: ConcurrentMap<Long, PokerRound>,
) {
    fun getNameOrUsername(message: Message): String {
        return message.from?.firstName.takeIf { it!!.isNotBlank() } ?: message.from?.username ?: ""
    }

    fun startNewRound(groupId: Long, playerInitiator: Long): Int {
        val status: Int
        if (rounds.containsKey(groupId)) {
            status = 0
        } else if (groupId == 0L || playerInitiator == 0L) {
            status = -1
        } else {
            val players = HashMap<Long, PlayerInRound>()

            rounds[groupId] = PokerRound(
                playerInitiator,
                isEnded = false,
                groupId,
                players,
                LocalDateTime.now(),
                actionCounter = 0,
            )
            status = 1
        }
        return status
    }

    suspend fun rollDices(message: Message, playerName: String): IntArray {
        var rollDices = intArrayOf()
        val groupId: Long = message.chat.id
        val playerId: Long = message.from!!.id
        if (checkRoundAvailable(groupId, playerId)) {
            val pr = rounds[groupId]
            pr!!.actionCounter = pr.actionCounter + 2

            if (!playerService.existsPlayer(playerId)) {
                playerService.addNewPlayer(
                    playerId,
                    username = message.from!!.username ?: "",
                    firstName = message.from!!.firstName,
                    lastName = message.from!!.lastName ?: "",
                )
            } else {
                playerService.checkAndUpdateFirstName(playerId, playerName)
            }

            rollDices = DiceUtil.roll5d6()

            val pir = playerService.createPiR()
            pir.name = playerName
            pir.dices = rollDices
            pir.isRoll = false
            pr.players[playerId] = pir
            pr.actionCounter -= 1
        }
        return rollDices
    }

    fun rerollDices(message: Message): Pair<IntArray, IntArray> {
        var firstRoll = intArrayOf()
        var reroll = intArrayOf()
        val groupId: Long = message.chat.id
        val playerId: Long = message.from?.id ?: 0
        if (checkRerollOrPassAvailable(groupId, playerId)) {
            val pattern = Pattern.compile(("^/" + Command.REROLL.value) + "(\\s+[1-6]){1,5}$")
            val matcher = pattern.matcher(message.text!!)
            if (matcher.matches()) {
                val pr = rounds[groupId]
                val pir = pr?.players?.get(playerId)
                reroll = StringUtil.getRerollNumbers(message.text!!)
                firstRoll = pir?.dices!!
                DiceUtil.reroll(firstRoll, reroll)
                pir.dices = firstRoll
                pir.isReroll = false
                pir.isPass = false
                pr.players[playerId] = pir
                pr.actionCounter -= 1
            }
        }
        return Pair(firstRoll, reroll)
    }

    fun pass(message: Message): Boolean {
        val groupId: Long = message.chat.id
        val playerId: Long = message.from!!.id
        val resultPassing: Boolean
        if (checkRerollOrPassAvailable(groupId, playerId)) {
            val pr = rounds[groupId]
            val pir = pr?.players?.get(playerId)
            if (pir != null) {
                pir.isReroll = false
                pir.isPass = false
                pr.players[playerId] = pir
                pr.actionCounter -= 1
            }
            resultPassing = true
        } else {
            resultPassing = false
        }
        return resultPassing
    }

    fun finishRound(message: Message): Boolean {
        val groupId: Long = message.chat.id
        val playerId: Long = message.from!!.id
        val resultFinishing: Boolean
        if (rounds.containsKey(groupId) && ((rounds[groupId]?.playerInitiator ?: 0) == playerId)) {
            val pr = rounds[groupId]
            if (pr != null) {
                pr.isEnded = true
            }
            rounds.remove(groupId)
            resultFinishing = true
        } else {
            resultFinishing = false
        }
        return resultFinishing
    }

    private fun checkRerollOrPassAvailable(groupId: Long, playerId: Long): Boolean {
        if (!rounds.containsKey(groupId)) return false
        val pr = rounds[groupId]
        return pr != null && pr.players.containsKey(playerId) &&
            !pr.isEnded && !pr.players[playerId]?.isRoll!! &&
            pr.players[playerId]?.isReroll!! && pr.players[playerId]?.isPass!!
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

    fun checkAvailableActions(groupId: Long): Boolean {
        val pr = rounds[groupId]
        return pr?.actionCounter == 0 && pr.players.size > 1
    }

    suspend fun saveResultsAndDeleteRound(groupId: Long): Map<Long, RoundResult> {
        val pr = rounds[groupId]
        var result: Map<Long, RoundResult> = mapOf()
        if (pr != null) {
            result = scoreService.processingRoundResult(pr)
            if (pr.players.size > 1) {
                val winner = result.entries
                    .stream()
                    .sorted(comparingByValue(DiceUtil::customComparator))
                    .findFirst()
                    .orElseThrow()
                    .key
                resultService.addNewResult(groupId, winner)
            }
            rounds.remove(pr.groupId)
        }
        return result
    }

    suspend fun getLeaderBoardByGroup(groupId: Long): Pair<Int, Map<String, Int>> {
        val results: List<Result> = resultService.findByGroupIdAndRoundTimeBetween(
            groupId,
            LocalDateTime.now().minusDays(7),
            LocalDateTime.now(),
        )
        val leaders: Map<String, Int> =
            results.groupingBy { it.player.firstName.ifBlank { it.player.username } }.eachCount()

        return Pair(results.size, leaders)
    }
}
