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

@Component
@DependsOn("liquibase")
class NotificationScheduler(
    private val taskRepository: TaskRepository,
    private val notificationRepository: NotificationRepository,
    private val notificationFactory: NotificationFactory,
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
        return notificationFactory.composeForTask(task)
            .concatMap { notificationRepository.save(it) }
            .then()
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}