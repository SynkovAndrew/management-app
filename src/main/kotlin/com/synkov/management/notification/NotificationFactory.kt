package com.synkov.management.notification

import com.synkov.management.task.Task
import com.synkov.management.task.TaskLabel
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.util.*

@Component
class NotificationFactory {

    fun composeForTask(task: Task): Flux<Notification> {
        return Mono.justOrEmpty(task.due?.datetime)
            .flatMapMany { eventAt ->
                task.labels.toFlux()
                    .filter { it != TaskLabel.SYNCHRONIZED }
                    .map { it.remindAt(eventAt) }
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
}