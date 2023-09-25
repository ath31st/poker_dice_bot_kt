package org.example.botfarm.entity

class PlayerInRound(
    val name: String,
    var isRoll: Boolean,
    var isReroll: Boolean,
    var isPass: Boolean,
    val dices: IntArray,
    var score: Int,
)