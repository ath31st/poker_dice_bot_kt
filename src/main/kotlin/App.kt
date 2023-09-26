package org.example.botfarm

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.telegramError
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.logging.LogLevel
import org.example.botfarm.util.Command
import org.example.botfarm.util.MessageEnum
import org.slf4j.LoggerFactory

object AppKt {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 1. bot token
    @JvmStatic
    fun main(args: Array<String>) {
        DatabaseFactory.init()

        logger.info("application starting...")
        val botToken = args[0]

        val bot = bot {
            logLevel = LogLevel.Error
            token = botToken
            dispatch {
                command(Command.START.value) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(update.message!!.chat.id),
                        text = "Started!"
                    )
                }
                command(Command.ROLL.value) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(update.message!!.chat.id),
                        text = "Roll!"
                    )
                }
                command(Command.REROLL.value) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(update.message!!.chat.id),
                        text = "Reroll!"
                    )
                }
                command(Command.PASS.value) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(update.message!!.chat.id),
                        text = "Pass!"
                    )
                }
                command(Command.FINISH.value) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(update.message!!.chat.id),
                        text = "Finish!"
                    )
                }
                command(Command.HELP.value) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(update.message!!.chat.id),
                        parseMode = ParseMode.MARKDOWN,
                        text = MessageEnum.HELP.value
                    )
                }
                command(Command.COMBINATION.value) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(update.message!!.chat.id),
                        parseMode = ParseMode.MARKDOWN,
                        text = MessageEnum.COMBINATION.value
                    )
                }
                command(Command.STATISTICS.value) {
                    bot.sendMessage(
                        chatId = ChatId.fromId(update.message!!.chat.id),
                        text = "Stat!"
                    )
                }
                telegramError {
                    println(error.getErrorMessage())
                }
            }
        }

        bot.startPolling()
        logger.info("bot successfully started")
    }
}
