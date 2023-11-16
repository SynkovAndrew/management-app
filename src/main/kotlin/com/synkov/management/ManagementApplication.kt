package com.synkov.management

import com.synkov.management.telegram.ReminderBot
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@SpringBootApplication
class ManagementApplication

fun main(args: Array<String>) {
    val context = runApplication<ManagementApplication>(*args)

    try {
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        telegramBotsApi.registerBot(context.getBean(ReminderBot::class.java))
    } catch (e: TelegramApiException) {
        throw RuntimeException(e)
    }
}
