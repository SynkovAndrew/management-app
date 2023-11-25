package com.synkov.management.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import org.springframework.stereotype.Component

@Component
class TelegramClient(
    private val properties: TelegramProperties
) {
    private val telegramBot = TelegramBot(properties.token)

    fun sendMessage(message: String) {
        telegramBot.execute(SendMessage(properties.chatId, message))
    }
}
