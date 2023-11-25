package com.synkov.management

import com.synkov.management.telegram.TelegramClient
import com.synkov.management.task.TodoistClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ManagementApplicationTests @Autowired constructor(
    private val todoistClient: TodoistClient,
    private val telegramClient: TelegramClient
) {

    @Test
    fun contextLoads() {
        val a = todoistClient.findTasks().collectList().block()

        println()
    }
}
