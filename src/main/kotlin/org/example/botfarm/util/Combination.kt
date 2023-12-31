package org.example.botfarm.util

/**
 * Enum representing different combinations in a poker round.
 *
 * @property value The human-readable name of the combination.
 * @property priority The priority of the combination, used for scoring.
 */
enum class Combination(val value: String, val priority: Int) {
    POKER("Покер", 9),
    SQUARE("Каре", 8),
    FULL_HOUSE("Фулл-хаус", 7),
    LARGE_STRAIGHT("Большой стрейт", 6),
    SMALL_STRAIGHT("Малый стрейт", 5),
    SET("Сет", 4),
    TWO_PAIR("Две пары", 3),
    PAIR("Пара", 2),
    NOTHING("Ничего", 1),
}
