package com.synkov.management.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class ReminderController {

    @GetMapping("api/v1/reminder")
    fun findAll(): Flux<Reminder> {
        return Flux.fromIterable(
            listOf(
                Reminder("First"),
                Reminder("Second"),
                Reminder("Third"),
            )
        )
    }
}

data class Reminder(
    val content: String
)