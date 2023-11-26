package com.synkov.management.rest

import com.synkov.management.telegram.TelegramClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class TestController(
    private val telegramClient: TelegramClient
) {

    @GetMapping("api/v1/telegram/send")
    fun findAll(): Mono<Void> {
        return Mono.fromRunnable { telegramClient.sendMessage("Test") }
    }
}
