package com.synkov.management.notification

import com.synkov.management.task.Task
import com.synkov.management.task.TaskRepository
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.Disposables
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@Component
@DependsOn("liquibase")
class NotificationScheduler(
    private val taskRepository: TaskRepository,
    private val notificationRepository: NotificationRepository,
    private val transactionalOperator: TransactionalOperator
) {
    private val disposable = Disposables.composite()

    @PreDestroy
    fun destroy() {
        disposable.dispose()
    }

    @PostConstruct
    fun start() {
        val subscription = Flux.interval(Duration.ofSeconds(5))
            .onBackpressureDrop()
            .doOnSubscribe { log.info("Notification scheduler started") }
            .doFinally { log.info("Notification scheduler stopped") }
            .concatMap {
                taskRepository.findNextNotProcessed()
                    .filter { it.due?.datetime != null }
                    .flatMap {
                        createNotifications(it)
                            .then(taskRepository.update(it.process()))
                            .doOnError { error -> log.error("Failed to schedule notifications for $it", error) }
                            .doOnSuccess { _ -> log.info("Notifications for {} scheduled", it) }
                    }
                    .`as`(transactionalOperator::transactional)
                    .onErrorResume { Mono.empty() }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
        disposable.add(subscription)
    }

    private fun createNotifications(task: Task): Mono<Void> {
        return Mono.justOrEmpty(task.due?.datetime)
            .map { it.offsetToZone(task.due!!.timezone!!) }
            .flatMapMany { eventAt ->
                Flux.fromIterable(createNotifyAt(eventAt,task.due!!.timezone!!))
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
            .concatMap { notificationRepository.save(it) }
            .then()
    }

    private fun createNotifyAt(eventAt: LocalDateTime, zone: String): List<LocalDateTime> {
        val now = LocalDateTime.now(ZoneId.of("UTC")).offsetToZone(zone)
        return when {
            eventAt.minusDays(3) > now -> listOf(
                eventAt.minusMinutes(30),
                LocalDateTime.of(
                    eventAt.minusDays(1).toLocalDate(),
                    LocalTime.of(19, 0)
                ),
                LocalDateTime.of(
                    eventAt.minusDays(3).toLocalDate(),
                    LocalTime.of(19, 0)
                )
            )

            eventAt.minusDays(3) < now && now < eventAt.minusDays(1) -> listOf(
                eventAt.minusMinutes(30),
                eventAt.minusHours(4),
                LocalDateTime.of(
                    eventAt.minusDays(1).toLocalDate(),
                    LocalTime.of(19, 0)
                )
            )

            eventAt.minusDays(1) < now && now < eventAt.minusHours(1) -> listOf(
                eventAt.minusMinutes(30),
                eventAt.minusHours(4),
            )

            else -> listOf()
        }
    }

    private fun LocalDateTime.offsetToZone(zone: String) : LocalDateTime {
        return this.atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(ZoneId.of(zone))
            .toLocalDateTime()
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}