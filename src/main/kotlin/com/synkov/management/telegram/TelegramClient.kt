package com.synkov.management.telegram

import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.ParseMode
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
    private val properties: TelegramProperties
) {
    private val telegramBot = TelegramBot(properties.token)

    fun sendMessage(message: NotificationMessage) {
        val sendMessage = SendMessage(properties.chatId, message.asFormattedMessage())
            .parseMode(ParseMode.Markdown)
        telegramBot.execute(
            sendMessage,
            object : Callback<SendMessage, SendResponse> {
                override fun onResponse(request: SendMessage, response: SendResponse) {
                    log.info("{} sent to tg successfully", message)
                }

                override fun onFailure(request: SendMessage, e: IOException) {
                    log.error("Failed to send $message to tg", e)
                    telegramBot.execute(sendMessage)
                }
            }
        )
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    data class NotificationMessage(
        val title: String,
        val description: String,
        val eventAt: LocalDateTime
    ) {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu HH:mm")

        fun asFormattedMessage(): String {
            return buildString {
                append("```Reminder\n")
                append("$title\n\n")
                append("${dateTimeFormatter.format(eventAt)}\n\n")
                if (description.isNotEmpty()) append(description)
                append("```")
            }
        }
    }
}

