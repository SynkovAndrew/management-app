package com.synkov.management.notification

import com.synkov.management.telegram.TelegramClient
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.Disposables
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.LocalDateTime

@Component
class NotificationProcessor(
    private val notificationRepository: NotificationRepository,
    private val transactionalOperator: TransactionalOperator,
    private val telegramClient: TelegramClient
) {
    private val disposable = Disposables.composite()

    @PreDestroy
    fun destroy() {
        disposable.dispose()
    }

   // @PostConstruct
    fun start() {
        val subscription = Flux.interval(Duration.ofSeconds(5))
            .doOnSubscribe { log.info("Notification processor started") }
            .doFinally { log.info("Notification processor stopped") }
            .concatMap {
                notificationRepository
                    .findByTimestampAfterAndCompleted(LocalDateTime.now(), false)
                    .doOnNext { log.info("{} is processing ...", it) }
                    .concatMap { notification ->
                        Mono.just(notification)
                            .doOnNext { telegramClient.sendMessage(getMessage(it)) }
                            .flatMap { notificationRepository.complete(it.id) }
                            .doOnSuccess { log.info("{} is processed successfully", notification) }
                    }
                    .`as`(transactionalOperator::transactional)
                    .doOnError { log.error("Error occurred", it) }
                    .onErrorResume { Mono.empty() }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
        disposable.add(subscription)
    }

    private fun getMessage(notification: Notification) : String {
        return with(notification) {
            "REMIND: $timestamp - $content"
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}