package org.example.botfarm.entity


/**
 * Represents a player's state in a round of the game.
 *
 * @property name     The name of the player.
 * @property isRoll   Indicates whether the player has performed a roll action in the round.
 * @property isReroll Indicates whether the player has performed a reroll action in the round.
 * @property isPass   Indicates whether the player has passed their turn in the round.
 * @property dices    An array of integers representing the player's rolled dice values.
 * @property score    The player's score for the current round.
 */
class PlayerInRound(
    var name: String,
    var isRoll: Boolean,
    var isReroll: Boolean,
    var isPass: Boolean,
    var dices: IntArray,
    var score: Int,
)
