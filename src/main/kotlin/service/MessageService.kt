package org.example.botfarm.service

import org.example.botfarm.util.MessageEnum
import org.example.botfarm.util.RandomPhrase
import org.example.botfarm.util.StringUtil

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
}