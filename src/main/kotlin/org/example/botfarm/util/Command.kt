package org.example.botfarm.util

/**
 * Enum representing different commands used in the poker game.
 *
 * @property value The string value of the command.
 */
enum class Command(val value: String) {
    START("poker"),
    ROLL("roll"),
    REROLL("reroll"),
    PASS("pass"),
    FINISH("finish"),
    HELP("help"),
    COMBINATION("combo"),
    STATISTICS("stat")
}
