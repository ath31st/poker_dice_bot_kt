package org.example.botfarm.util

import java.security.SecureRandom
import java.util.Date
import org.example.botfarm.entity.RoundResult

/**
 * The `DiceUtil` object provides utility methods for working with dice rolls and scoring in the poker game.
 */
object DiceUtil {
    /**
     * Generates a random integer between the specified minimum and maximum values (inclusive).
     *
     * @param min The minimum value.
     * @param max The maximum value.
     * @return A random integer within the specified range.
     */
    private fun generateRandomInteger(min: Int, max: Int): Int {
        val random = SecureRandom()
        random.setSeed(Date().time)
        return random.nextInt((max - min) + 1) + min
    }

    /**
     * Rolls five six-sided dice and returns the results as an integer array.
     *
     * @return An array of length 5 containing the rolled dice values.
     */
    fun roll5d6(): IntArray {
        val arr = IntArray(5)
        for (i in arr.indices) {
            arr[i] = generateRandomInteger(1, 6)
        }
        arr.sort()
        return arr
    }

    /**
     * Rerolls selected dice in the first roll array based on the reroll array.
     *
     * @param firstRoll The first roll of dice values.
     * @param reroll The selected dice values to reroll.
     */
    fun reroll(firstRoll: IntArray, reroll: IntArray) {
        reroll.sort()
        var point = 0
        for (k in reroll) {
            for (j in point until firstRoll.size) {
                point++
                if (firstRoll[j] == k) {
                    firstRoll[j] = generateRandomInteger(1, 6)
                    break
                }
            }
        }
        firstRoll.sort()
    }

    /**
     * Checks if the dice values form a poker combination.
     *
     * @param dices The array of dice values.
     * @return `true` if it's a poker combination, `false` otherwise.
     */
    fun isPoker(dices: IntArray): Boolean {
        val temp = IntArray(5) { dices[0] }
        return temp.contentEquals(dices)
    }

    /**
     * Checks if the dice values form a full house combination.
     *
     * @param dices The array of dice values.
     * @return `true` if it's a full house combination, `false` otherwise.
     */
    fun isFullHouse(dices: IntArray): Boolean {
        val map = dices.groupBy { it }.mapValues { it.value.size.toLong() }
        return map.size == 2 && map.values.any { it > 1 }
    }

    /**
     * Checks if the dice values form a large straight combination.
     *
     * @param dices The array of dice values.
     * @return `true` if it's a large straight combination, `false` otherwise.
     */
    fun isLargeStraight(dices: IntArray): Boolean {
        val largeStraight = intArrayOf(2, 3, 4, 5, 6)
        return dices.contentEquals(largeStraight)
    }

    /**
     * Checks if the dice values form a small straight combination.
     *
     * @param dices The array of dice values.
     * @return `true` if it's a small straight combination, `false` otherwise.
     */
    fun isSmallStraight(dices: IntArray): Boolean {
        val smallStraight = intArrayOf(1, 2, 3, 4, 5)
        return dices.contentEquals(smallStraight)
    }

    /**
     * Checks if the dice values form a sequence of a specified length.
     *
     * @param dices The array of dice values.
     * @param seq The desired length of the sequence.
     * @return `true` if it's a sequence of the specified length, `false` otherwise.
     */
    fun isSequence(dices: IntArray, seq: Int): Boolean {
        var maxCount = 0
        var count = 1
        for (i in 1 until dices.size) {
            if (dices[i - 1] == dices[i]) {
                count++
                if (maxCount < count) maxCount = count
            } else {
                count = 1
            }
        }
        return maxCount == seq
    }

    /**
     * Calculates the score of a sequence of a specified length in the dice values.
     *
     * @param dices The array of dice values.
     * @param seq The desired length of the sequence.
     * @return The score of the sequence.
     */
    fun sequenceScore(dices: IntArray, seq: Int): Int {
        var repeatNumber = 0
        var maxCount = 0
        var count = 1
        for (i in 1 until dices.size) {
            if (dices[i - 1] == dices[i]) {
                count++
                if (maxCount < count) {
                    repeatNumber = dices[i]
                    maxCount = count
                }
            } else {
                count = 1
            }
        }
        return repeatNumber * seq
    }

    /**
     * Checks if the dice values form a two-pair combination.
     *
     * @param dices The array of dice values.
     * @return `true` if it's a two-pair combination, `false` otherwise.
     */
    fun isTwoPair(dices: IntArray): Boolean {
        val counts = IntArray(6)
        var pairs = 0
        for (dice in dices) {
            counts[dice - 1]++
        }
        for (value in counts) {
            if (value == 2) {
                pairs++
            }
        }
        return pairs == 2
    }

    /**
     * Calculates the score of a two-pair combination in the dice values.
     *
     * @param dices The array of dice values.
     * @return The score of the two-pair combination.
     */
    fun scoreTwoPair(dices: IntArray): Int {
        val map = dices.groupBy { it }.mapValues { it.value.size.toLong() }
        return map.entries
            .filter { it.value > 1 }
            .sumOf { it.key }
    }

    /**
     * Custom comparator for sorting [RoundResult] objects by priority and score.
     *
     * @param r1 The first [RoundResult].
     * @param r2 The second [RoundResult].
     * @return A negative integer if `r1` is less than `r2`, zero if they are equal, and a positive integer otherwise.
     */
    fun customComparator(r1: RoundResult, r2: RoundResult): Int {
        return when {
            r1.priority < r2.priority -> 1
            r1.priority == r2.priority -> r2.score.compareTo(r1.score)
            else -> -1
        }
    }
}
