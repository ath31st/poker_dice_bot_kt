package org.example.botfarm.util

/**
 * The `StringUtil` object provides utility functions for working with strings in the poker bot.
 */
object StringUtil {

    /**
     * Parses a string command and extracts an array of integers, typically used for rerolling dice numbers.
     *
     * @param command The input command string that contains space-separated integers.
     * @return An array of integers extracted from the command.
     */
    fun getRerollNumbers(command: String): IntArray {
        return command.substring(command.indexOf(" "))
            .trim()
            .replace("\\s+".toRegex(), " ")
            .split(" ")
            .map { it.toInt() }
            .toIntArray()
    }

    /**
     * Formats an integer array into a string with brackets separating the values.
     *
     * @param array The integer array to format.
     * @return A string representation of the array with values enclosed in brackets.
     */
    fun resultWithBrackets(array: IntArray): String {
        return "[" + array.joinToString("] [") + "]"
    }
}
