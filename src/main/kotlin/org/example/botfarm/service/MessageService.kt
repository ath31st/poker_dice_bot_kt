package org.example.botfarm.service

import java.util.Map.Entry.comparingByValue
import java.util.stream.Collectors
import org.example.botfarm.entity.PlayerInRound
import org.example.botfarm.entity.RoundResult
import org.example.botfarm.util.DiceUtil
import org.example.botfarm.util.MessageEnum
import org.example.botfarm.util.RandomPhrase
import org.example.botfarm.util.StringUtil

/**
 * The `MessageService` class provides methods for preparing text messages related to a game or round.
 */
class MessageService {

    /**
     * Prepares a text message after starting a round based on the startRoundStatus.
     *
     * @param startRoundStatus The status code indicating the result of starting the round.
     * @param playerName The name of the player.
     * @return A formatted text message.
     */
    fun prepareTextAfterStartingRound(startRoundStatus: Int, playerName: String): String {
        return when (startRoundStatus) {
            1 -> {
                MessageEnum.START_ROUND.value.format(playerName)
            }

            -1 -> {
                MessageEnum.ISSUE_WITH_GROUP_ID_OR_PLAYER_ID.value
            }

            else -> {
                MessageEnum.TABLE_BUSY.value
            }
        }
    }

    /**
     * Prepares a text message after rolling dices, including the player's name and the dice results.
     *
     * @param rollDices An array containing the rolled dice values.
     * @param playerName The name of the player.
     * @return A formatted text message.
     */
    fun prepareTextAfterRollDices(rollDices: IntArray?, playerName: String): String {
        val text = if (rollDices == null || rollDices.isEmpty()) {
            ""
        } else {
            RandomPhrase.getRollDicesPhrase()
                .format(playerName, StringUtil.resultWithBrackets(rollDices))
        }
        return text
    }

    /**
     * Prepares a text message after rerolling dices, including the player's name and the new and original dice results.
     *
     * @param firstRoll An array containing the original dice values.
     * @param reroll An array containing the new dice values after rerolling.
     * @param playerName The name of the player.
     * @return A formatted text message.
     */
    fun prepareTextAfterRerollDices(
        firstRoll: IntArray,
        reroll: IntArray,
        playerName: String,
    ): String {
        val text = if (firstRoll.isEmpty() || reroll.isEmpty()) {
            ""
        } else {
            RandomPhrase.getRerollPhrase().format(
                playerName,
                StringUtil.resultWithBrackets(reroll),
                StringUtil.resultWithBrackets(firstRoll),
            )
        }
        return text
    }

    /**
     * Prepares a text message after a player decides to pass their turn.
     *
     * @param playerName The name of the player.
     * @return A formatted text message.
     */
    fun prepareTextAfterPass(playerName: String): String {
        return RandomPhrase.getPassPhrase().format(playerName)
    }

    /**
     * Prepares a text message after finishing a round, including the player's name.
     *
     * @param playerName The name of the player.
     * @return A formatted text message.
     */
    fun prepareTextAfterFinishRound(playerName: String): String {
        return MessageEnum.FINISH_ROUND.value.format(playerName)
    }

    /**
     * Prepares a text message displaying the results of a round.
     *
     * @param result A map containing round results with player IDs as keys and RoundResult objects as values.
     * @param players A map containing player information with player IDs as keys and PlayerInRound objects as values.
     * @return A formatted text message displaying round results.
     */
    fun prepareResultText(
        result: Map<Long, RoundResult>,
        players: Map<Long, PlayerInRound>,
    ): String {
        return "=====================\nРезультаты раунда:\n```" + "\n" +
                result.entries
                    .stream()
                    .sorted(comparingByValue(DiceUtil::customComparator))
                    .map { (key, value): Map.Entry<Long, RoundResult> ->
                        "${players[key]?.name}: ${value.combination.value} {${value.score}}"
                    }.collect(Collectors.joining("\n")) + "```"
    }

    /**
     * Prepares a text message displaying the leaderboard for the game group.
     *
     * @param roundsCount The total number of rounds played.
     * @param leaders A map containing player names as keys and their scores as values.
     * @return A formatted text message displaying the leaderboard.
     */
    fun prepareLeaderBoardText(roundsCount: Int, leaders: Map<String, Int>): String {
        return "=====================\n```\nЗа прошедшую неделю сыграно: $roundsCount раунда(ов).\nТаблица лидеров этой группы (Топ 5):\n" +
                leaders.entries
                    .stream()
                    .sorted(comparingByValue(Comparator.reverseOrder()))
                    .limit(5)
                    .map { (key, value): Map.Entry<String, Int> -> "$key: {$value}" }
                    .collect(Collectors.joining("\n")) + "```"
    }

    /**
     * Prepares a text message for auto-passing a player's turn.
     *
     * @param playerName The name of the player.
     * @return A formatted text message for auto-passing.
     */
    fun prepareAutoPassText(playerName: String): String {
        return RandomPhrase.getAutoPassPhrase().format(playerName)
    }
}
