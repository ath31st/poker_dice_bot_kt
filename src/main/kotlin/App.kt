package org.example.botfarm

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.telegramError
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.logging.LogLevel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.example.botfarm.entity.PokerRound
import org.example.botfarm.scheduler.PokerDiceScheduler
import org.example.botfarm.service.MessageService
import org.example.botfarm.service.PlayerService
import org.example.botfarm.service.ResultService
import org.example.botfarm.service.RoundService
import org.example.botfarm.service.ScoreService
import org.example.botfarm.util.Command
import org.example.botfarm.util.MessageEnum
import org.slf4j.LoggerFactory

object AppKt {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val rounds: ConcurrentMap<Long, PokerRound> = ConcurrentHashMap()
    private val roundService =
        RoundService(PlayerService(), ResultService(), ScoreService(), rounds)
    private val messageService = MessageService()
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

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
                start()
                roll()
                reroll()
                pass()
                finish()
                help()
                combination()
                statistics()
                scheduler()
                error()
            }
        }

        bot.startPolling()
        logger.info("bot successfully started")
    }

    private fun Dispatcher.error() {
        telegramError {
            println(error.getErrorMessage())
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun Dispatcher.scheduler() {
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
                                text = MessageEnum.TIME_EXPIRED.value,
                            )
                            it.second.forEach { playerName ->
                                bot.sendMessage(
                                    chatId = ChatId.fromId(groupId),
                                    parseMode = ParseMode.MARKDOWN,
                                    text = messageService.prepareAutoPassText(playerName),
                                )
                            }
                            bot.sendMessage(
                                chatId = ChatId.fromId(groupId),
                                parseMode = ParseMode.MARKDOWN,
                                text = messageService.prepareResultText(
                                    result,
                                    round!!.players,
                                ),
                            )
                        }
                    }
                }
            }, 10, 15, TimeUnit.SECONDS)
        }
    }

    private fun Dispatcher.help() {
        command(Command.HELP.value) {
            val groupId = update.message?.chat?.id ?: 0
            bot.sendMessage(
                chatId = ChatId.fromId(groupId),
                parseMode = ParseMode.MARKDOWN,
                text = MessageEnum.HELP.value,
            )
        }
    }

    private fun Dispatcher.finish() {
        command(Command.FINISH.value) {
            if (roundService.finishRound(message)) {
                val groupId = update.message?.chat?.id ?: 0
                val playerName = roundService.getNameOrUsername(message)
                bot.sendMessage(
                    chatId = ChatId.fromId(groupId),
                    text = messageService.prepareTextAfterFinishRound(playerName),
                )
            }
        }
    }

    private fun Dispatcher.pass() {
        command(Command.PASS.value) {
            val groupId = update.message?.chat?.id ?: 0
            val playerName = roundService.getNameOrUsername(message)
            if (roundService.pass(message)) {
                bot.sendMessage(
                    chatId = ChatId.fromId(groupId),
                    text = messageService.prepareTextAfterPass(playerName),
                )
            }
            if (roundService.checkAvailableActions(groupId)) {
                val round = rounds[groupId]
                val result = roundService.saveResultsAndDeleteRound(groupId)
                bot.sendMessage(
                    chatId = ChatId.fromId(groupId),
                    parseMode = ParseMode.MARKDOWN,
                    text = messageService.prepareResultText(
                        result,
                        round!!.players,
                    ),
                )
            }
        }
    }

    private fun Dispatcher.reroll() {
        command(Command.REROLL.value) {
            val groupId = update.message?.chat?.id ?: 0
            val playerName = roundService.getNameOrUsername(message)
            val rolls = roundService.rerollDices(message)
            bot.sendMessage(
                chatId = ChatId.fromId(groupId),
                text = messageService.prepareTextAfterRerollDices(
                    rolls.first,
                    rolls.second,
                    playerName,
                ),
            )
            if (roundService.checkAvailableActions(groupId)) {
                val round = rounds[groupId]
                val result = roundService.saveResultsAndDeleteRound(groupId)
                bot.sendMessage(
                    chatId = ChatId.fromId(groupId),
                    parseMode = ParseMode.MARKDOWN,
                    text = messageService.prepareResultText(
                        result,
                        round!!.players,
                    ),
                )
            }
        }
    }

    private fun Dispatcher.roll() {
        command(Command.ROLL.value) {
            val groupId = update.message?.chat?.id ?: 0
            val playerName = roundService.getNameOrUsername(message)
            val rollDices = update.message?.let { roundService.rollDices(it, playerName) }
            bot.sendMessage(
                chatId = ChatId.fromId(groupId),
                text = messageService.prepareTextAfterRollDices(rollDices, playerName),
            )
        }
    }

    private fun Dispatcher.start() {
        command(Command.START.value) {
            val groupId = update.message?.chat?.id ?: 0
            val playerInitiator = update.message?.from?.id ?: 0
            val playerName = roundService.getNameOrUsername(message)
            val startRoundStatus = roundService.startNewRound(groupId, playerInitiator)
            bot.sendMessage(
                chatId = ChatId.fromId(groupId),
                text = messageService.prepareTextAfterStartingRound(
                    startRoundStatus,
                    playerName,
                ),
            )
        }
    }

    private fun Dispatcher.combination() {
        command(Command.COMBINATION.value) {
            val groupId = update.message?.chat?.id ?: 0
            bot.sendMessage(
                chatId = ChatId.fromId(groupId),
                parseMode = ParseMode.MARKDOWN,
                text = MessageEnum.COMBINATION.value,
            )
        }
    }

    private fun Dispatcher.statistics() {
        command(Command.STATISTICS.value) {
            val groupId = update.message?.chat?.id ?: 0
            val leaders = roundService.getLeaderBoardByGroup(groupId)
            bot.sendMessage(
                chatId = ChatId.fromId(groupId),
                parseMode = ParseMode.MARKDOWN,
                text = messageService.prepareLeaderBoardText(leaders.first, leaders.second),
            )
        }
    }
}
