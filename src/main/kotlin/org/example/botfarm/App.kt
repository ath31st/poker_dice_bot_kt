package org.example.botfarm

import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.message.MarkdownParseMode
import dev.inmo.tgbotapi.utils.RiskFeature
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
    @OptIn(RiskFeature::class)
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

                onCommand(Command.COMBINATION.value) {
                    sendTextMessage(
                        chatId = it.chat.id,
                        disableNotification = true,
                        parseMode = MarkdownParseMode,
                        text = MessageEnum.COMBINATION.value,
                    )
                }

                onCommand(Command.START.value) {
                    val groupId = it.chat.id.chatId
                    val playerInitiator = it.from?.id?.chatId ?: 0
                    val playerName = roundService.getNameOrUsername(it.from)
                    val startRoundStatus = roundService.startNewRound(groupId, playerInitiator)
                    sendTextMessage(
                        chatId = it.chat.id,
                        disableNotification = true,
                        parseMode = MarkdownParseMode,
                        text = messageService.prepareTextAfterStartingRound(
                            startRoundStatus,
                            playerName,
                        ),
                    )
                }

                onCommand(Command.STATISTICS.value) {
                    val groupId = it.chat.id.chatId
                    val leaders = roundService.getLeaderBoardByGroup(groupId)
                    sendTextMessage(
                        chatId = it.chat.id,
                        disableNotification = true,
                        parseMode = MarkdownParseMode,
                        text = messageService.prepareLeaderBoardText(leaders.first, leaders.second),
                    )
                }

                onCommand(Command.ROLL.value) {
                    val playerName = roundService.getNameOrUsername(it.from)
                    val rollDices = roundService.rollDices(it, playerName)
                    sendTextMessage(
                        chatId = it.chat.id,
                        disableNotification = true,
                        text = messageService.prepareTextAfterRollDices(rollDices, playerName),
                    )
                }

                onCommandWithArgs(Command.REROLL.value) { message, _ ->
                    val groupId = message.chat.id.chatId
                    val playerName = roundService.getNameOrUsername(message.from)
                    val playerId = message.from?.id?.chatId ?: 0
                    val rolls = roundService.rerollDices(groupId, playerId, message.content.text)
                    sendTextMessage(
                        chatId = message.chat.id,
                        disableNotification = true,
                        text = messageService.prepareTextAfterRerollDices(
                            rolls.first,
                            rolls.second,
                            playerName,
                        ),
                    )
                    if (roundService.checkAvailableActions(groupId)) {
                        val round = rounds[groupId]
                        val result = roundService.saveResultsAndDeleteRound(groupId)
                        sendTextMessage(
                            chatId = message.chat.id,
                            disableNotification = true,
                            parseMode = MarkdownParseMode,
                            text = messageService.prepareResultText(
                                result,
                                round!!.players,
                            ),
                        )
                    }
                }

                onCommand(Command.PASS.value) {
                    val groupId = it.chat.id.chatId
                    val playerName = roundService.getNameOrUsername(it.from)
                    val playerId = it.from?.id?.chatId ?: 0
                    if (roundService.pass(groupId, playerId)) {
                        sendTextMessage(
                            chatId = it.chat.id,
                            disableNotification = true,
                            text = messageService.prepareTextAfterPass(playerName),
                        )
                    }
                    if (roundService.checkAvailableActions(groupId)) {
                        val round = rounds[groupId]
                        val result = roundService.saveResultsAndDeleteRound(groupId)
                        sendTextMessage(
                            chatId = it.chat.id,
                            disableNotification = true,
                            parseMode = MarkdownParseMode,
                            text = messageService.prepareResultText(
                                result,
                                round!!.players,
                            ),
                        )
                    }
                }

                onCommand(Command.FINISH.value) {
                    val groupId = it.chat.id.chatId
                    val playerId = it.from?.id?.chatId ?: 0
                    if (roundService.finishRound(groupId, playerId)) {
                        val playerName = roundService.getNameOrUsername(it.from)
                        sendTextMessage(
                            chatId = it.chat.id,
                            disableNotification = true,
                            text = messageService.prepareTextAfterFinishRound(playerName),
                        )
                    }
                }

                onText {
                    scheduler()
                }
            }.join()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun BehaviourContext.scheduler() {
        scheduler.scheduleAtFixedRate({
            GlobalScope.launch {
                val autoCloseableRounds = PokerDiceScheduler.finalizeRounds(rounds)
                if (autoCloseableRounds.isNotEmpty()) {
                    autoCloseableRounds.forEach {
                        val groupId = it.first
                        val round = rounds[groupId]
                        val result = roundService.saveResultsAndDeleteRound(groupId)
                        sendTextMessage(
                            chatId = ChatId(groupId),
                            disableNotification = true,
                            parseMode = MarkdownParseMode,
                            text = MessageEnum.TIME_EXPIRED.value,
                        )
                        it.second.forEach { playerName ->
                            sendTextMessage(
                                chatId = ChatId(groupId),
                                disableNotification = true,
                                parseMode = MarkdownParseMode,
                                text = messageService.prepareAutoPassText(playerName),
                            )
                        }
                        sendTextMessage(
                            chatId = ChatId(groupId),
                            disableNotification = true,
                            parseMode = MarkdownParseMode,
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
