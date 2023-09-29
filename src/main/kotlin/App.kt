package org.example.botfarm

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.telegramError
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.logging.LogLevel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.example.botfarm.entity.PokerRound
import org.example.botfarm.scheduler.PokerDiceScheduler
import org.example.botfarm.service.*
import org.example.botfarm.util.Command
import org.example.botfarm.util.MessageEnum
import org.slf4j.LoggerFactory
import java.util.concurrent.*

object AppKt {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val rounds: ConcurrentMap<Long, PokerRound> = ConcurrentHashMap()
    private val roundService =
        RoundService(PlayerService(), ResultService(), ScoreService(), rounds)
    private val messageService = MessageService()
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    // 1. bot token
    @OptIn(DelicateCoroutinesApi::class)
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
                    val groupId = update.message!!.chat.id
                    val playerInitiator = update.message!!.from!!.id
                    val playerName = roundService.getNameOrUsername(message)
                    val isSuccessfulStart = roundService.startNewRound(groupId, playerInitiator)
                    bot.sendMessage(
                        chatId = ChatId.fromId(groupId),
                        text = messageService.prepareTextAfterStartingRound(
                            isSuccessfulStart,
                            playerName
                        )
                    )
                }
                command(Command.ROLL.value) {
                    val playerName = roundService.getNameOrUsername(message)
                    val rollDices = roundService.rollDices(update.message!!, playerName)
                    bot.sendMessage(
                        chatId = ChatId.fromId(update.message!!.chat.id),
                        text = messageService.prepareTextAfterRollDices(rollDices, playerName)
                    )
                }
                command(Command.REROLL.value) {
                    val playerName = roundService.getNameOrUsername(message)
                    val groupId = update.message!!.chat.id
                    val round = rounds[groupId]
                    val rolls = roundService.rerollDices(message)
                    bot.sendMessage(
                        chatId = ChatId.fromId(groupId),
                        text = messageService.prepareTextAfterRerollDices(
                            rolls.first,
                            rolls.second,
                            playerName
                        )
                    )
                    if (roundService.checkAvailableActions(groupId)) {
                        val result = roundService.saveResultsAndDeleteRound(groupId)
                        bot.sendMessage(
                            chatId = ChatId.fromId(groupId),
                            parseMode = ParseMode.MARKDOWN,
                            text = messageService.prepareResultText(
                                result,
                                round!!.players
                            )
                        )
                    }
                }
                command(Command.PASS.value) {
                    val groupId = update.message!!.chat.id
                    val playerName = roundService.getNameOrUsername(message)
                    if (roundService.pass(message)) {
                        bot.sendMessage(
                            chatId = ChatId.fromId(update.message!!.chat.id),
                            text = messageService.prepareTextAfterPass(playerName)
                        )
                    }
                    if (roundService.checkAvailableActions(groupId)) {
                        val result = roundService.saveResultsAndDeleteRound(groupId)
                        val round = rounds[groupId]
                        bot.sendMessage(
                            chatId = ChatId.fromId(groupId),
                            parseMode = ParseMode.MARKDOWN,
                            text = messageService.prepareResultText(
                                result,
                                round!!.players
                            )
                        )
                    }
                }
                command(Command.FINISH.value) {
                    if (roundService.finishRound(message)) {
                        val playerName = roundService.getNameOrUsername(message)
                        bot.sendMessage(
                            chatId = ChatId.fromId(update.message!!.chat.id),
                            text = messageService.prepareTextAfterFinishRound(playerName)
                        )
                    }
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
                    val groupId = update.message!!.chat.id
                    val leaders = roundService.getLeaderBoardByGroup(groupId)
                    bot.sendMessage(
                        chatId = ChatId.fromId(groupId),
                        parseMode = ParseMode.MARKDOWN,
                        text = messageService.prepareLeaderBoardText(leaders.first, leaders.second)
                    )
                }
                text {
                    scheduler.scheduleAtFixedRate({
                        GlobalScope.launch {
                            val autoCloseableRounds = PokerDiceScheduler.finalizeRounds(rounds)
                            if (autoCloseableRounds.isNotEmpty()) {
                                autoCloseableRounds.forEach {
                                    val groupId = it.first
                                    val round = rounds[groupId]
                                    val result = roundService.saveResultsAndDeleteRound(groupId)
                                    bot.sendMessage(
                                        chatId = ChatId.fromId(groupId),
                                        parseMode = ParseMode.MARKDOWN,
                                        text = MessageEnum.TIME_EXPIRED.value
                                    )
                                    it.second.forEach { playerName ->
                                        bot.sendMessage(
                                            chatId = ChatId.fromId(groupId),
                                            parseMode = ParseMode.MARKDOWN,
                                            text = messageService.prepareAutoPassText(playerName)
                                        )
                                    }
                                    bot.sendMessage(
                                        chatId = ChatId.fromId(groupId),
                                        parseMode = ParseMode.MARKDOWN,
                                        text = messageService.prepareResultText(
                                            result,
                                            round!!.players
                                        )
                                    )

                                }
                            }
                        }
                    }, 10, 15, TimeUnit.SECONDS)
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
