package com.synkov.management.notification

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@Component
interface NotificationRepository : ReactiveCrudRepository<Notification, UUID> {

    fun findByTimestampAfterAndCompleted(timestamp: LocalDateTime, completed: Boolean): Flux<Notification>

    fun countByExternalId(externalId: String): Mono<Long>

    @Query("UPDATE notification SET completed = true WHERE id = :notificationId")
    fun complete(notificationId: UUID): Mono<UUID>
}
