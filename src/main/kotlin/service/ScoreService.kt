package org.example.botfarm.service

import org.example.botfarm.entity.PlayerInRound
import org.example.botfarm.entity.PokerRound
import org.example.botfarm.entity.RoundResult
import org.example.botfarm.util.Combination
import org.example.botfarm.util.DiceUtil.isFullHouse
import org.example.botfarm.util.DiceUtil.isLargeStraight
import org.example.botfarm.util.DiceUtil.isPoker
import org.example.botfarm.util.DiceUtil.isSequence
import org.example.botfarm.util.DiceUtil.isSmallStraight
import org.example.botfarm.util.DiceUtil.isTwoPair
import org.example.botfarm.util.DiceUtil.scoreTwoPair
import org.example.botfarm.util.DiceUtil.sequenceScore
import java.util.*

class ScoreService {

    fun processingRoundResult(pr: PokerRound): Map<Long, RoundResult> {
        val result: MutableMap<Long, RoundResult> = HashMap()

        pr.players.forEach { (key: Long, value: PlayerInRound) ->
            result[key] = getRoundResult(value.dices)
        }
        return result
    }

    private fun getRoundResult(dices: IntArray): RoundResult {
        return when {
            isPoker(dices) -> {
                RoundResult(
                    dices[0] * 5,
                    Combination.POKER,
                    Combination.POKER.priority
                )
            }

            isSequence(dices, 4) -> {
                RoundResult(
                    dices[2] * 4,
                    Combination.SQUARE,
                    Combination.SQUARE.priority
                )
            }

            isFullHouse(dices) -> {
                RoundResult(
                    Arrays.stream(dices).sum(),
                    Combination.FULL_HOUSE,
                    Combination.FULL_HOUSE.priority
                )
            }

            isLargeStraight(dices) -> {
                RoundResult(
                    Arrays.stream(dices).sum(),
                    Combination.LARGE_STRAIGHT,
                    Combination.LARGE_STRAIGHT.priority
                )
            }

            isSmallStraight(dices) -> {
                RoundResult(
                    Arrays.stream(dices).sum(),
                    Combination.SMALL_STRAIGHT,
                    Combination.SMALL_STRAIGHT.priority
                )
            }

            isSequence(dices, 3) -> {
                RoundResult(
                    sequenceScore(dices, 3),
                    Combination.SET,
                    Combination.SET.priority
                )
            }

            isTwoPair(dices) -> {
                RoundResult(
                    scoreTwoPair(dices),
                    Combination.TWO_PAIR,
                    Combination.TWO_PAIR.priority
                )
            }

            isSequence(dices, 2) -> {
                RoundResult(
                    sequenceScore(dices, 2),
                    Combination.PAIR,
                    Combination.PAIR.priority
                )
            }

            else -> {
                RoundResult(
                    score = 0,
                    Combination.NOTHING,
                    Combination.NOTHING.priority
                )
            }
        }
    }
}