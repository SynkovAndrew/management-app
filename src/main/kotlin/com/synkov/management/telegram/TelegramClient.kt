package com.synkov.management.telegram

import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.SendResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class TelegramClient(
    private val properties: TelegramProperties,
    telegramBotFactory: SingletonTelegramBotFactory
) {
    private val telegramBot = telegramBotFactory.getSingleton()

    fun sendMessage(message: Message) {
        SendMessage(properties.chatId, message.asFormattedMessage())
            .parseMode(ParseMode.Markdown)
            .let {
                telegramBot.execute(
                    it,
                    object : Callback<SendMessage, SendResponse> {
                        override fun onResponse(request: SendMessage, response: SendResponse) {
                            log.info("{} sent to tg successfully", message)
                        }

                        override fun onFailure(request: SendMessage, e: IOException) {
                            log.error("Failed to send $message to tg", e)
                            telegramBot.execute(it)
                        }
                    }
                )
            }
    }

    fun sendButtons() {
        val keboard = ReplyKeyboardMarkup(
            listOf("First button", "Second Button").toTypedArray(),
            listOf("First button", "Second Button").toTypedArray(),
        ).oneTimeKeyboard(true).resizeKeyboard(true).selective(true)
        telegramBot.execute(SendMessage(properties.chatId, "").replyMarkup(keboard))
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    data class NotificationMessage(
        val title: String,
        val description: String,
        val remindAt: LocalDateTime,
        val notifyAt: LocalDateTime,
        val showRemindAt: Boolean = false
    ) : Message {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu HH:mm")

        override fun asFormattedMessage(): String {
            return buildString {
                append("```Reminder\n")
                if (showRemindAt) append("Remind At ${dateTimeFormatter.format(remindAt)}\n\n")
                append("$title\n\n")
                if (description.isNotEmpty()) append("${description}\n\n")
                append("At ${dateTimeFormatter.format(notifyAt)}")
                append("```")
            }
        }
    }

    data class TaskMessage(
        val id: String,
        val isCompleted: Boolean,
        val content: String,
        val description: String,
        val due: LocalDateTime
    ) : Message {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu HH:mm")

        override fun asFormattedMessage(): String {
            return buildString {
                append("```Task\n")
                append("Id: $id\n\n")
                append("${"Completed".takeIf { isCompleted } ?: "Not completed"}\n\n")
                append("$content\n\n")
                if (description.isNotEmpty()) append("${description}\n\n")
                append(dateTimeFormatter.format(due))
                append("```")
            }
        }
    }

    interface Message {
        fun asFormattedMessage(): String
    }
}
