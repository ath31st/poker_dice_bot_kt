package org.example.botfarm.util

import org.example.botfarm.entity.RoundResult
import java.security.SecureRandom
import java.util.*

object DiceUtil {
    private fun generateRandomInteger(min: Int, max: Int): Int {
        val random = SecureRandom()
        random.setSeed(Date().time)
        return random.nextInt((max - min) + 1) + min
    }

    fun roll5d6(): IntArray {
        val arr = IntArray(5)
        for (i in arr.indices) {
            arr[i] = generateRandomInteger(1, 6)
        }
        arr.sort()
        return arr
    }

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

    fun isPoker(dices: IntArray): Boolean {
        val temp = IntArray(5) { dices[0] }
        return temp.contentEquals(dices)
    }

    fun isFullHouse(dices: IntArray): Boolean {
        val map = dices.groupBy { it }.mapValues { it.value.size.toLong() }
        return map.size == 2 && map.values.any { it > 1 }
    }

    fun isLargeStraight(dices: IntArray): Boolean {
        val largeStraight = intArrayOf(2, 3, 4, 5, 6)
        return dices.contentEquals(largeStraight)
    }

    fun isSmallStraight(dices: IntArray): Boolean {
        val smallStraight = intArrayOf(1, 2, 3, 4, 5)
        return dices.contentEquals(smallStraight)
    }

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

    fun scoreTwoPair(dices: IntArray): Int {
        val map = dices.groupBy { it }.mapValues { it.value.size.toLong() }
        return map.entries
            .filter { it.value > 1 }
            .sumOf { it.key }
    }

    fun customComparator(r1: RoundResult, r2: RoundResult): Int {
        return when {
            r1.priority < r2.priority -> 1
            r1.priority == r2.priority -> r2.score.compareTo(r1.score)
            else -> -1
        }
    }
}
