package org.example.botfarm.entity

import kotlinx.datetime.LocalDateTime

data class PokerRound(
    val playerInitiator: Long,
    var isEnded: Boolean,
    val groupId: Long,
    val players: MutableMap<Long, PlayerInRound>,
    val startRound: LocalDateTime,
    var actionCounter: Int,
)
