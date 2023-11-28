package com.synkov.management.notification

import com.synkov.management.task.Task
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@Component
class NotificationFactory {

    fun composeForTask(task: Task): Flux<Notification> {
        return Mono.justOrEmpty(task.due?.datetime)
            .flatMapMany { eventAt ->
                Flux
                    .fromIterable(
                        if (task.due!!.isRecurring) {
                            createNotifyAtForRecurring(eventAt)
                        } else {
                            createNotifyAt(eventAt)
                        }
                    )
                    .map {
                        Notification(
                            UUID.randomUUID(),
                            task.id,
                            task.content,
                            task.description,
                            eventAt,
                            it,
                            false
                        )
                    }
            }
    }

    private fun createNotifyAtForRecurring(eventAt: LocalDateTime): List<LocalDateTime> {
        val now = LocalDateTime.now()
        return when {
            eventAt.minusHours(48) >= now -> listOf(
                eventAt.minusMinutes(30),
                LocalDateTime.of(
                    eventAt.minusDays(1).toLocalDate(),
                    LocalTime.of(19, 0)
                )
            )

            eventAt.minusHours(48) <= now && now <= eventAt.minusHours(1) -> listOf(
                eventAt.minusMinutes(30),
                eventAt.minusHours(4),
            )

            else -> listOf()
        }
    }

    private fun createNotifyAt(eventAt: LocalDateTime): List<LocalDateTime> {
        val now = LocalDateTime.now()
        return when {
            // more than 3 days to event
            eventAt.minusDays(3) >= now -> listOf(
                eventAt.minusMinutes(30),
                eventAt.minusHours(4),
                LocalDateTime.of(
                    eventAt.minusDays(1).toLocalDate(),
                    LocalTime.of(19, 0)
                ),
                LocalDateTime.of(
                    eventAt.minusDays(3).toLocalDate(),
                    LocalTime.of(19, 0)
                )
            )

            // from 3 to 2 days to event
            eventAt.minusDays(3) <= now && now <= eventAt.minusDays(2) -> listOf(
                eventAt.minusMinutes(30),
                eventAt.minusHours(4),
                LocalDateTime.of(
                    eventAt.minusDays(1).toLocalDate(),
                    LocalTime.of(19, 0)
                )
            )

            // from 48 to 40 hours to event
            eventAt.minusHours(48) <= now && now <= eventAt.minusHours(40) -> listOf(
                eventAt.minusMinutes(30),
                eventAt.minusHours(4),
                eventAt.minusHours(39)
            )

            // from 40 to 32 hours to event
            eventAt.minusHours(40) <= now && now <= eventAt.minusHours(32) -> listOf(
                eventAt.minusMinutes(30),
                eventAt.minusHours(4),
                eventAt.minusHours(31)
            )

            // from 32 to 24 hours to event
            eventAt.minusHours(32) <= now && now <= eventAt.minusHours(24) -> listOf(
                eventAt.minusMinutes(30),
                eventAt.minusHours(4),
                eventAt.minusHours(23)
            )

            // from 24 to 18 hours to event
            eventAt.minusHours(24) <= now && now <= eventAt.minusHours(18) -> listOf(
                eventAt.minusMinutes(30),
                eventAt.minusHours(4),
                eventAt.minusHours(23)
            )

            // from 18 to 10 hours to event
            eventAt.minusHours(18) <= now && now <= eventAt.minusHours(10) -> listOf(
                eventAt.minusMinutes(30),
                eventAt.minusHours(4),
                eventAt.minusHours(9)
            )

            // from 10 to 2 hours to event
            eventAt.minusHours(10) <= now && now <= eventAt.minusHours(2) -> listOf(
                eventAt.minusMinutes(30),
                eventAt.minusHours(2)
            )

            // less than 2 hours to event
            eventAt.minusHours(2) <= now -> listOf(
                eventAt.minusMinutes(30)
            )

            else -> listOf()
        }
    }
}