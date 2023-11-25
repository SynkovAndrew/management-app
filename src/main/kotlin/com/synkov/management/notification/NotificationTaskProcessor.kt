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
class NotificationTaskProcessor(
    private val notificationTaskRepository: NotificationTaskRepository,
    private val transactionalOperator: TransactionalOperator,
    private val telegramClient: TelegramClient
) {
    private val disposable = Disposables.composite()

    @PreDestroy
    fun destroy() {
        disposable.dispose()
    }

    @PostConstruct
    fun start() {
        val subscription = Flux.interval(Duration.ofSeconds(5))
            .doOnSubscribe { NotificationTaskScheduler.log.info("Notification task processor started") }
            .doFinally { NotificationTaskScheduler.log.info("Notification task processor stopped") }
            .concatMap {
                notificationTaskRepository
                    .findByTimestampAfterAndCompleted(LocalDateTime.now(), false)
                    .doOnNext { log.info("{} is processing ...", it) }
                    .concatMap { notificationTask ->
                        Mono.just(notificationTask)
                            .doOnNext { telegramClient.sendMessage(getMessage(it)) }
                            .flatMap { notificationTaskRepository.complete(it.id) }
                            .doOnSuccess { log.info("{} is processed successfully", notificationTask) }
                    }
                    .`as`(transactionalOperator::transactional)
                    .doOnError { log.error("Error occurred", it) }
                    .onErrorResume { Mono.empty() }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
        disposable.add(subscription)
    }

    private fun getMessage(notificationTask: NotificationTask) : String {
        return with(notificationTask) {
            "REMIND: $timestamp - $content"
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}