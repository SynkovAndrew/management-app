package com.synkov.management.telegram

enum class TelegramBotCommand(val text: String) {
    TASKS("/tasks"),
    NOTIFICATIONS("/notifications"),
    MENU("/menu");

    companion object {
        fun fromText(text: String): TelegramBotCommand? {
            return TelegramBotCommand.values()
                .firstOrNull { it.text == text }
        }
    }
}
