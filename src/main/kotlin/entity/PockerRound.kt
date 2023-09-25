package org.example.botfarm.entity

import kotlinx.datetime.LocalDateTime

class PockerRound(
    val playerInitiator: Long,
    var isEnded: Boolean,
    val idChannel: Long,
    val players: MutableMap<Long, PlayerInRound>,
    val startRound: LocalDateTime,
    var actionCounter: Int,
)