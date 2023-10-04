package org.example.botfarm.util

import kotlin.random.Random

/**
 * The `RandomPhrase` object generates random phrases for different game events in the poker bot.
 */
object RandomPhrase {
    private val autoPass = listOf(
        "Угадайте, что снится %s? Автоматический пропуск хода! Принесите ему(ей) одеяло",
        "%s больше не наливать! Штрафной автоматический пропуск хода! И заберите у него(нее) бутылку",
        "%s ушел(шла) в себя! Что ж, это автоматический пропуск хода! Доиграем без него(нее)!",
        "Кто-нибудь видел %s? Негоже заставлять других ждать. Автоматический пропуск хода!",
        "Сейчас бы не доигрывать раунды, да, %s? Автоматический пропуск хода!",
        "По~~матросил~~ролил(а) и бросил(а) %s? Не надо так. Автоматический пропуск хода!",
    )

    private val rollDices = listOf(
        "%s ловко бросает кости %s",
        "%s сегодня, определенно, везет %s",
        "%s hehe, boy %s",
        "%s совершает героический бросок костей %s",
        "Поглядите на %s, до чего же хорош его(ее) бросок %s",
        "Ведите летописца, %s выбросил легендарные %s",
    )

    private val rerollDices = listOf(
        "%s с надеждой перебрасывает кости %s\nПолучилось %s",
        "%s дует на кости %s перед перебросом\nПолучилось %s",
        "%s недовольно смотрит на %s\nПолучилось %s",
        "%s мечтает, чтобы вместо %s были другие кости\nПолучилось %s",
        "%s тратит остатки удачи на %s\nПолучилось %s",
    )

    private val pass = listOf(
        "%s с ухмылкой пропускает ход",
        "%s загадочно потирает руки",
        "%s держит покерфейс",
        "%s наверняка рассчитывает на победу!",
        "%s что-то задумал(а)",
        "%s чешет левую пятку, перебрасывать не будет",
    )

    fun getAutoPassPhrase(): String {
        val index = Random.nextInt(autoPass.size)
        return autoPass[index]
    }

    fun getRollDicesPhrase(): String {
        val index = Random.nextInt(rollDices.size)
        return rollDices[index]
    }

    fun getPassPhrase(): String {
        val index = Random.nextInt(pass.size)
        return pass[index]
    }

    fun getRerollPhrase(): String {
        val index = Random.nextInt(rerollDices.size)
        return rerollDices[index]
    }
}
