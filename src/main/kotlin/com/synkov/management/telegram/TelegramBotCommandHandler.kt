package com.synkov.management.telegram

import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.synkov.management.notification.NotificationRepository
import com.synkov.management.task.TaskRepository
import org.springframework.stereotype.Component

@Component
class TelegramBotCommandHandler(
    telegramBotFactory: SingletonTelegramBotFactory,
    private val taskRepository: TaskRepository,
    private val notificationRepository: NotificationRepository,
    private val telegramClient: TelegramClient
) {
    private val telegramBot = telegramBotFactory.getSingleton()

    init {
        telegramBot.setUpdatesListener { updates ->
            handleUpdate(updates)
            UpdatesListener.CONFIRMED_UPDATES_ALL
        }
    }

    private fun handleUpdate(updates: List<Update>) {
        val telegramBotCommands = updates
            .mapNotNull { it.message().text() }
            .mapNotNull { TelegramBotCommand.fromText(it) }
        telegramBotCommands.forEach { handleCommand(it) }
    }

    private fun handleCommand(command: TelegramBotCommand) {
        when (command) {
            TelegramBotCommand.TASKS -> handleTasksCommand()
            TelegramBotCommand.NOTIFICATIONS -> handleNotificationsCommand()
            TelegramBotCommand.MENU -> handMenuCommand()
        }
    }

    private fun handleTasksCommand() {
        taskRepository.findAll()
            .collectList()
            .block()
            ?.forEach {
                telegramClient.sendMessage(
                    TelegramClient.TaskMessage(
                        it.id,
                        it.isCompleted,
                        it.content,
                        it.description,
                        it.due!!.datetime!!
                    )
                )
            }
    }

    private fun handleNotificationsCommand() {
        notificationRepository.findNotCompleted()
            .collectList()
            .block()
            ?.sortedBy { it.notifyAt }
            ?.forEach {
                telegramClient.sendMessage(
                    TelegramClient.NotificationMessage(
                        it.title,
                        it.description,
                        it.notifyAt,
                        it.eventAt,
                        showRemindAt = true
                    )
                )
            }
    }

    private fun handMenuCommand() {
        telegramClient.sendButtons()
    }
}