package org.example.botfarm

import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.message.MarkdownParseMode
import dev.inmo.tgbotapi.types.message.ParseMode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

/**
 * The `AppKt` object serves as the main entry point for the poker bot application.
 * It sets up the bot's functionalities and defines how it should respond to different commands and events.
 */
object AppKt {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val rounds: ConcurrentMap<Long, PokerRound> = ConcurrentHashMap()
    private val roundService =
        RoundService(PlayerService(), ResultService(), ScoreService(), rounds)
    private val messageService = MessageService()
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    /**
     * The main function for starting the poker bot application.
     *
     * @param args An array of command-line arguments, where the first argument should be the bot token.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        DatabaseFactory.init()
        logger.info("application starting...")
        val botToken = args.first()
        val bot = telegramBot(botToken)

        runBlocking {
            bot.buildBehaviourWithLongPolling {
                println(getMe())

                onCommand(Command.HELP.value) {
                    sendTextMessage(
                        chatId = it.chat.id,
                        disableNotification = true,
                        parseMode = MarkdownParseMode,
                        text = MessageEnum.HELP.value,
                    )
                }


            }.join()
        }
    }

//    /**
//     * Schedules automatic round finalizing and checks for rounds that have exceeded their time limit.
//     */
//    @OptIn(DelicateCoroutinesApi::class)
//    private fun Dispatcher.scheduler() {
//        text {
//            scheduler.scheduleAtFixedRate({
//                GlobalScope.launch {
//                    val autoCloseableRounds = PokerDiceScheduler.finalizeRounds(rounds)
//                    if (autoCloseableRounds.isNotEmpty()) {
//                        autoCloseableRounds.forEach {
//                            val groupId = it.first
//                            val round = rounds[groupId]
//                            val result = roundService.saveResultsAndDeleteRound(groupId)
//                            bot.sendMessage(
//                                chatId = ChatId.fromId(groupId),
//                                disableNotification = true,
//                                parseMode = ParseMode.MARKDOWN,
//                                text = MessageEnum.TIME_EXPIRED.value,
//                            )
//                            it.second.forEach { playerName ->
//                                bot.sendMessage(
//                                    chatId = ChatId.fromId(groupId),
//                                    disableNotification = true,
//                                    parseMode = ParseMode.MARKDOWN,
//                                    text = messageService.prepareAutoPassText(playerName),
//                                )
//                            }
//                            bot.sendMessage(
//                                chatId = ChatId.fromId(groupId),
//                                disableNotification = true,
//                                parseMode = ParseMode.MARKDOWN,
//                                text = messageService.prepareResultText(
//                                    result,
//                                    round!!.players,
//                                ),
//                            )
//                        }
//                    }
//                }
//            }, 10, 15, TimeUnit.SECONDS)
//        }
//    }
//
//    /**
//     * Handles the '/help' command by sending a help message with instructions to the group or chat.
//     */
//    private fun Dispatcher.help() {
//        command(Command.HELP.value) {
//            val groupId = update.message?.chat?.id ?: 0
//            bot.sendMessage(
//                chatId = ChatId.fromId(groupId),
//                disableNotification = true,
//                parseMode = ParseMode.MARKDOWN,
//                text = MessageEnum.HELP.value,
//            )
//        }
//    }
//
//    /**
//     * Handles the '/finish' command, allowing the round initiator to prematurely finish the round.
//     */
//    private fun Dispatcher.finish() {
//        command(Command.FINISH.value) {
//            if (roundService.finishRound(message)) {
//                val groupId = update.message?.chat?.id ?: 0
//                val playerName = roundService.getNameOrUsername(message)
//                bot.sendMessage(
//                    chatId = ChatId.fromId(groupId),
//                    disableNotification = true,
//                    text = messageService.prepareTextAfterFinishRound(playerName),
//                )
//            }
//        }
//    }
//
//    /**
//     * Handles the '/pass' command, allowing a player to pass their turn during a round.
//     */
//    private fun Dispatcher.pass() {
//        command(Command.PASS.value) {
//            val groupId = update.message?.chat?.id ?: 0
//            val playerName = roundService.getNameOrUsername(message)
//            if (roundService.pass(message)) {
//                bot.sendMessage(
//                    chatId = ChatId.fromId(groupId),
//                    disableNotification = true,
//                    text = messageService.prepareTextAfterPass(playerName),
//                )
//            }
//            if (roundService.checkAvailableActions(groupId)) {
//                val round = rounds[groupId]
//                val result = roundService.saveResultsAndDeleteRound(groupId)
//                bot.sendMessage(
//                    chatId = ChatId.fromId(groupId),
//                    disableNotification = true,
//                    parseMode = ParseMode.MARKDOWN,
//                    text = messageService.prepareResultText(
//                        result,
//                        round!!.players,
//                    ),
//                )
//            }
//        }
//    }
//
//    /**
//     * Handles the '/reroll' command, allowing a player to reroll selected dice during their turn.
//     */
//    private fun Dispatcher.reroll() {
//        command(Command.REROLL.value) {
//            val groupId = update.message?.chat?.id ?: 0
//            val playerName = roundService.getNameOrUsername(message)
//            val rolls = roundService.rerollDices(message)
//            bot.sendMessage(
//                chatId = ChatId.fromId(groupId),
//                disableNotification = true,
//                text = messageService.prepareTextAfterRerollDices(
//                    rolls.first,
//                    rolls.second,
//                    playerName,
//                ),
//            )
//            if (roundService.checkAvailableActions(groupId)) {
//                val round = rounds[groupId]
//                val result = roundService.saveResultsAndDeleteRound(groupId)
//                bot.sendMessage(
//                    chatId = ChatId.fromId(groupId),
//                    disableNotification = true,
//                    parseMode = ParseMode.MARKDOWN,
//                    text = messageService.prepareResultText(
//                        result,
//                        round!!.players,
//                    ),
//                )
//            }
//        }
//    }
//
//    /**
//     * Handles the '/roll' command, allowing a player to roll dice during their turn.
//     */
//    private fun Dispatcher.roll() {
//        command(Command.ROLL.value) {
//            val groupId = update.message?.chat?.id ?: 0
//            val playerName = roundService.getNameOrUsername(message)
//            val rollDices = update.message?.let { roundService.rollDices(it, playerName) }
//            bot.sendMessage(
//                chatId = ChatId.fromId(groupId),
//                disableNotification = true,
//                text = messageService.prepareTextAfterRollDices(rollDices, playerName),
//            )
//        }
//    }
//
//    /**
//     * Handles the '/start' command, allowing a player to initiate a new poker round.
//     */
//    private fun Dispatcher.start() {
//        command(Command.START.value) {
//            val groupId = update.message?.chat?.id ?: 0
//            val playerInitiator = update.message?.from?.id ?: 0
//            val playerName = roundService.getNameOrUsername(message)
//            val startRoundStatus = roundService.startNewRound(groupId, playerInitiator)
//            bot.sendMessage(
//                chatId = ChatId.fromId(groupId),
//                text = messageService.prepareTextAfterStartingRound(
//                    startRoundStatus,
//                    playerName,
//                ),
//            )
//        }
//    }
//
//    /**
//     * Handles the '/combination' command, providing a list of valid poker combinations.
//     */
//    private fun Dispatcher.combination() {
//        command(Command.COMBINATION.value) {
//            val groupId = update.message?.chat?.id ?: 0
//            bot.sendMessage(
//                chatId = ChatId.fromId(groupId),
//                disableNotification = true,
//                parseMode = ParseMode.MARKDOWN,
//                text = MessageEnum.COMBINATION.value,
//            )
//        }
//    }
//
//    /**
//     * Handles the '/statistics' command, providing the leaderboard for the current group or chat.
//     */
//    private fun Dispatcher.statistics() {
//        command(Command.STATISTICS.value) {
//            val groupId = update.message?.chat?.id ?: 0
//            val leaders = roundService.getLeaderBoardByGroup(groupId)
//            bot.sendMessage(
//                chatId = ChatId.fromId(groupId),
//                disableNotification = true,
//                parseMode = ParseMode.MARKDOWN,
//                text = messageService.prepareLeaderBoardText(leaders.first, leaders.second),
//            )
//        }
//    }
}
