package com.synkov.management.telegram

import com.pengrad.telegrambot.TelegramBot
import org.springframework.stereotype.Component

@Component
class SingletonTelegramBotFactory(properties: TelegramProperties, ) {
    private val telegramBot = TelegramBot(properties.token)

    fun getSingleton(): TelegramBot = telegramBot
}
