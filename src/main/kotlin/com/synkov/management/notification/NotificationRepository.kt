package com.synkov.management.notification

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@Component
interface NotificationRepository : ReactiveCrudRepository<Notification, UUID> {

    @Query(
        """SELECT * FROM notification 
           WHERE notify_at <= :now AND completed = false 
           LIMIT 1
           FOR UPDATE SKIP LOCKED"""
    )
    fun findNextNotCompleted(now: LocalDateTime): Mono<Notification>

    @Query("UPDATE notification SET completed = true WHERE id = :notificationId")
    fun complete(notificationId: UUID): Mono<UUID>
}
