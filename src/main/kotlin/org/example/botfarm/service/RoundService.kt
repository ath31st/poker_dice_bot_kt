package org.example.botfarm.service

import com.github.kotlintelegrambot.entities.Message
import java.time.LocalDateTime
import java.util.Map.Entry.comparingByValue
import java.util.concurrent.ConcurrentMap
import java.util.regex.Pattern
import org.example.botfarm.entity.PlayerInRound
import org.example.botfarm.entity.PokerRound
import org.example.botfarm.entity.Result
import org.example.botfarm.entity.RoundResult
import org.example.botfarm.util.Command
import org.example.botfarm.util.DiceUtil
import org.example.botfarm.util.StringUtil

/**
 * The `RoundService` class provides methods for managing poker rounds and game actions.
 *
 * @param playerService An instance of [PlayerService] for player-related operations.
 * @param resultService An instance of [ResultService] for game result operations.
 * @param scoreService An instance of [ScoreService] for scoring game rounds.
 * @param rounds A concurrent map to store active poker rounds.
 */
class RoundService(
    private val playerService: PlayerService,
    private val resultService: ResultService,
    private val scoreService: ScoreService,
    private val rounds: ConcurrentMap<Long, PokerRound>,
) {
    companion object {
        private const val DAYS: Long = 7
        private const val NEW_PLAYER_ACTIONS = 2
    }

    /**
     * Retrieves the name or username from a Telegram message.
     *
     * @param message The Telegram message.
     * @return The first name if available, or the username if not, or an empty string.
     */
    fun getNameOrUsername(message: Message): String {
        return message.from?.firstName.takeIf { it!!.isNotBlank() } ?: message.from?.username ?: ""
    }

    /**
     * Starts a new poker round for a group initiated by a player.
     *
     * @param groupId The unique ID of the group.
     * @param playerInitiator The unique ID of the player initiating the round.
     * @return Status codes: 1 for success, 0 for existing round, -1 for invalid parameters.
     */
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

    /**
     * Rolls five dice for a player in the specified group.
     *
     * @param message The Telegram message.
     * @param playerName The name of the player.
     * @return An array of integers representing the rolled dice values.
     */
    suspend fun rollDices(message: Message, playerName: String): IntArray {
        var rollDices = intArrayOf()
        val groupId: Long = message.chat.id
        val playerId: Long = message.from!!.id
        if (checkRoundAvailable(groupId, playerId)) {
            val pr = rounds[groupId]
            pr!!.actionCounter = pr.actionCounter + NEW_PLAYER_ACTIONS

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

    /**
     * Rerolls selected dice for a player in the specified group.
     *
     * @param message The Telegram message containing reroll instructions.
     * @return A pair of integer arrays representing the first roll and rerolled dice values.
     */
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

    /**
     * Passes a player's turn in the specified group.
     *
     * @param message The Telegram message indicating the player's intention to pass.
     * @return `true` if the pass is successful, `false` otherwise.
     */
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

    /**
     * Finishes the current poker round initiated by a player in the specified group.
     *
     * @param message The Telegram message indicating the player's intention to finish the round.
     * @return `true` if the round is successfully finished, `false` otherwise.
     */
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

    /**
     * Checks if reroll or pass actions are available for a player in the specified group.
     *
     * @param groupId The unique ID of the group.
     * @param playerId The unique ID of the player.
     * @return `true` if reroll or pass actions are available, `false` otherwise.
     */
    private fun checkRerollOrPassAvailable(groupId: Long, playerId: Long): Boolean {
        if (!rounds.containsKey(groupId)) return false
        val pr = rounds[groupId]
        return pr != null && pr.players.containsKey(playerId) &&
                !pr.isEnded && !pr.players[playerId]?.isRoll!! &&
                pr.players[playerId]?.isReroll!! && pr.players[playerId]?.isPass!!
    }

    /**
     * Checks if a poker round is available for a player in the specified group.
     *
     * @param groupId The unique ID of the group.
     * @param playerId The unique ID of the player.
     * @return `true` if a poker round is available, `false` otherwise.
     */
    private fun checkRoundAvailable(groupId: Long, playerId: Long): Boolean {
        val result: Boolean = if (!rounds.containsKey(groupId)) {
            false
        } else {
            val pr = rounds[groupId]
            !pr!!.isEnded && !pr.players.containsKey(playerId)
        }
        return result
    }

    /**
     * Checks if available actions have been exhausted in the specified group.
     *
     * @param groupId The unique ID of the group.
     * @return `true` if available actions are exhausted and there are more than one player, `false` otherwise.
     */
    fun checkAvailableActions(groupId: Long): Boolean {
        val pr = rounds[groupId]
        return pr?.actionCounter == 0 && pr.players.size > 1
    }

    /**
     * Saves the results of a poker round, processes the winner, and deletes the round.
     *
     * @param groupId The unique ID of the group.
     * @return A map of player IDs and their corresponding round results.
     */
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

    /**
     * Retrieves the leaderboard for a group based on recent game results.
     *
     * @param groupId The unique ID of the group.
     * @return A pair containing the total number of rounds played and a map of player names and their scores.
     */
    suspend fun getLeaderBoardByGroup(groupId: Long): Pair<Int, Map<String, Int>> {
        val results: List<Result> = resultService.findByGroupIdAndRoundTimeBetween(
            groupId,
            LocalDateTime.now().minusDays(DAYS),
            LocalDateTime.now(),
        )
        val leaders: Map<String, Int> =
            results.groupingBy { it.player.firstName.ifBlank { it.player.username } }.eachCount()

        return Pair(results.size, leaders)
    }
}
