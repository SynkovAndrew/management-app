package com.synkov.management.telegram

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("telegram")
data class TelegramProperties(
    val token: String,
    val bot: String,
    val chatId: String
)
