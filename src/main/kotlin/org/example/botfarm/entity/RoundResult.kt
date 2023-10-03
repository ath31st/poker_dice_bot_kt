package org.example.botfarm.entity

import org.example.botfarm.util.Combination

/**
 * Represents the result of a round in the game.
 *
 * @property score       The score achieved in the round.
 * @property combination The combination of dice values that led to this result.
 * @property priority    The priority of this result, used for ranking in the game.
 */
class RoundResult(
    val score: Int,
    val combination: Combination,
    val priority: Int,
)
