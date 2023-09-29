package org.example.botfarm.util

object StringUtil {

    fun getRerollNumbers(command: String): IntArray {
        return command.substring(command.indexOf(" "))
            .trim()
            .replace("\\s+".toRegex(), " ")
            .split(" ")
            .map { it.toInt() }
            .toIntArray()
    }

    fun resultWithBrackets(array: IntArray): String {
        return "[" + array.joinToString("] [") + "]"
    }
}
