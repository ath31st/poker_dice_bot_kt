package org.example.botfarm.entity

import java.time.LocalDateTime


data class PokerRound(
    val playerInitiator: Long,
    var isEnded: Boolean,
    val groupId: Long,
    val players: MutableMap<Long, PlayerInRound>,
    val startRound: LocalDateTime,
    var actionCounter: Int,
)
