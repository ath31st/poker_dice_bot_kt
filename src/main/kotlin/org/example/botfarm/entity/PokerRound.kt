package org.example.botfarm.entity

import java.time.LocalDateTime

/**
 * Represents a round of the poker game.
 *
 * @property playerInitiator The unique identifier of the player who initiated the round.
 * @property isEnded         Indicates whether the round has ended.
 * @property groupId         The unique identifier of the group to which this round belongs.
 * @property players         A mutable map of player identifiers to their respective states in the round.
 * @property startRound      The date and time when the round started.
 * @property actionCounter   The counter for tracking the number of actions taken in the round.
 */
data class PokerRound(
    val playerInitiator: Long,
    var isEnded: Boolean,
    val groupId: Long,
    val players: MutableMap<Long, PlayerInRound>,
    val startRound: LocalDateTime,
    var actionCounter: Int,
)
