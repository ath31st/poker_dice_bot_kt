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
    fun prepareTextAfterStartingRound(isSuccessStart: Boolean, playerName: String): String {
        return if (isSuccessStart) {
            MessageEnum.START_ROUND.value.format(playerName)
        } else {
            MessageEnum.TABLE_BUSY.value
        }
    }

    fun prepareTextAfterRollDices(rollDices: IntArray, playerName: String): String {
        val text = if (rollDices.isEmpty()) {
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
        playerName: String
    ): String {
        val text = if (firstRoll.isEmpty() || reroll.isEmpty()) {
            ""
        } else {
            String.format(
                RandomPhrase.getRerollPhrase(),
                playerName,
                StringUtil.resultWithBrackets(reroll),
                StringUtil.resultWithBrackets(firstRoll)
            )
        }
        return text
    }

    fun prepareResultText(
        result: Map<Long, RoundResult>,
        players: Map<Long, PlayerInRound>
    ): String {
        return """
     =====================
     Результаты раунда:
     ```
     """.trimIndent() +
                result.entries
                    .stream()
                    .sorted(comparingByValue(DiceUtil::customComparator))
                    .map { (key, value): Map.Entry<Long, RoundResult> ->
                        "${players[key]?.name}: ${value.combination.value} {${value.score}}"
                    }
                    .collect(Collectors.joining("\n")) + "```"
    }
}