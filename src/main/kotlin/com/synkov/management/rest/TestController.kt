package com.synkov.management.rest

import com.synkov.management.telegram.TelegramClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@RestController
class TestController(
    private val telegramClient: TelegramClient
) {

    @GetMapping("api/v1/telegram/send")
    fun findAll(): Mono<Void> {
        return Mono.fromRunnable {
            telegramClient.sendMessage(
                TelegramClient.NotificationMessage(
                    "Don't forget to buy a thing",
                    "There is a good thing you should buy. It's located near to your home.",
                    LocalDateTime.now(),
                    LocalDateTime.now()
                )
            )
        }
    }
}
