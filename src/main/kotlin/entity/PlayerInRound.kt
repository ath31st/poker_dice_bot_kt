package org.example.botfarm.entity

class PlayerInRound(
    var name: String,
    var isRoll: Boolean,
    var isReroll: Boolean,
    var isPass: Boolean,
    var dices: IntArray,
    var score: Int,
)
