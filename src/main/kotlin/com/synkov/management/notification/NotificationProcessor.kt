package com.synkov.management.notification

import com.synkov.management.telegram.TelegramClient
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
@DependsOn("liquibase")
class NotificationProcessor(
    private val notificationRepository: NotificationRepository,
    private val transactionalOperator: TransactionalOperator,
    private val telegramClient: TelegramClient
) {
    private val disposable = Disposables.composite()
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu HH:mm")

    @PreDestroy
    fun destroy() {
        disposable.dispose()
    }

    @PostConstruct
    fun start() {
        val subscription = Flux.interval(Duration.ofSeconds(5))
            .onBackpressureDrop()
            .doOnSubscribe { log.info("Notification processor started") }
            .doFinally { log.info("Notification processor stopped") }
            .concatMap {
                notificationRepository
                    .findNextNotCompleted(nowAtZone("Europe/Moscow"))
                    .doOnNext { log.info("{} is notifying ...", it) }
                    .flatMap { notification ->
                        Mono.just(notification)
                            .doOnNext { telegramClient.sendMessage(getMessage(it)) }
                            .flatMap { notificationRepository.complete(it.id) }
                            .doOnError { log.error("Failed to notify", it) }
                            .doOnSuccess { log.info("{} is notified successfully", notification) }
                    }
                    .`as`(transactionalOperator::transactional)
                    .onErrorResume { Mono.empty() }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
        disposable.add(subscription)
    }

    private fun getMessage(notification: Notification): String {
        return with(notification) {
            buildString {
                append("EVENT: $title\n\n")
                append("WHEN: ${dateTimeFormatter.format(eventAt)}\n\n")
                if (description.isNotEmpty()) append("DESCRIPTION: $description\n\n")
                append("-------------------------------------------------")
            }
        }
    }

    private fun nowAtZone(zone: String): LocalDateTime {
        return ZoneId.of("UTC").let { utcZone ->
            LocalDateTime.now(utcZone)
                .atZone(utcZone)
                .withZoneSameInstant(ZoneId.of(zone))
                .toLocalDateTime()
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}