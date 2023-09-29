package org.example.botfarm.service

import org.example.botfarm.entity.PlayerInRound
import org.example.botfarm.entity.RoundResult
import org.example.botfarm.util.DiceUtil
import org.example.botfarm.util.MessageEnum
import org.example.botfarm.util.RandomPhrase
import org.example.botfarm.util.StringUtil
import java.util.Map.Entry.comparingByValue
import java.util.stream.Collectors

class MessageService {
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

    fun prepareTextAfterRollDices(rollDices: IntArray?, playerName: String): String {
        val text = if (rollDices == null || rollDices.isEmpty()) {
            ""
        } else {
            RandomPhrase.getRollDicesPhrase()
                .format(playerName, StringUtil.resultWithBrackets(rollDices))
        }
        return text
    }

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

    fun prepareTextAfterPass(playerName: String): String {
        return RandomPhrase.getPassPhrase().format(playerName)
    }

    fun prepareTextAfterFinishRound(playerName: String): String {
        return MessageEnum.FINISH_ROUND.value.format(playerName)
    }

    fun prepareResultText(
        result: Map<Long, RoundResult>,
        players: Map<Long, PlayerInRound>,
    ): String {
        return "=====================\nРезультаты раунда:\n```\n".trimIndent() +
            result.entries
                .stream()
                .sorted(comparingByValue(DiceUtil::customComparator))
                .map { (key, value): Map.Entry<Long, RoundResult> ->
                    "${players[key]?.name}: ${value.combination.value} {${value.score}}"
                }.collect(Collectors.joining("\n")) + "```"
    }

    fun prepareLeaderBoardText(roundsCount: Int, leaders: Map<String, Int>): String {
        return "=====================\n```\nЗа прошедшую неделю сыграно: $roundsCount раунда(ов).\nТаблица лидеров этой группы (Топ 5):\n" +
            leaders.entries
                .stream()
                .sorted(comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map { (key, value): Map.Entry<String, Int> -> "$key: {$value}" }
                .collect(Collectors.joining("\n")) + "```"
    }

    fun prepareAutoPassText(playerName: String): String {
        return RandomPhrase.getAutoPassPhrase().format(playerName)
    }
}
