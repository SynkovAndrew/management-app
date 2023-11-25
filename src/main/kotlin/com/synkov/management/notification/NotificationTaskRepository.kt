package com.synkov.management.notification

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@Component
interface NotificationTaskRepository : ReactiveCrudRepository<NotificationTask, UUID> {

    fun findByTimestampAfterAndCompleted(timestamp: LocalDateTime, completed: Boolean): Flux<NotificationTask>

    fun countByExternalId(externalId: String): Mono<Long>

    @Query("UPDATE notification_task SET completed = true WHERE id = :notificationTaskId")
    fun complete(notificationTaskId: UUID): Mono<UUID>
}
