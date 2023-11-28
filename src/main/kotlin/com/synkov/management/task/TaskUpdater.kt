package com.synkov.management.task

import com.synkov.management.notification.NotificationFactory
import com.synkov.management.notification.NotificationRepository
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

@Component
@DependsOn("liquibase")
class TaskUpdater(
    private val todoistClient: TodoistClient,
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
            .doOnSubscribe { log.info("Task updater started") }
            .doFinally { log.info("Task updater stopped") }
            .concatMap {
                taskRepository.findAll()
                    .concatMap { task ->
                        todoistClient.findTask(task.id)
                            .filter { !TaskEqualityTester.test(it, task) }
                            .flatMap {
                                updateTaskAndNotifications(it, task.due?.datetime)
                                    .doOnSuccess { _ -> log.info("Task(id={}) updated", it.id) }
                            }
                            .`as`(transactionalOperator::transactional)
                            .doOnError { error -> log.error("Task updater failed", error) }
                    }
                    .onErrorResume { Mono.empty() }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
        disposable.add(subscription)
    }

    private fun updateTaskAndNotifications(task: Task, oldDateTime: LocalDateTime?): Mono<Task> {
        return taskRepository.update(task)
            .filter { it.due?.datetime != oldDateTime }
            .flatMap { updateNotifications(it) }
            .defaultIfEmpty(task)
    }

    private fun updateNotifications(task: Task): Mono<Task> {
        return notificationRepository.completeForTask(task.id)
            .flatMapMany { notificationFactory.composeForTask(task) }
            .flatMap { notificationRepository.save(it) }
            .collectList()
            .thenReturn(task)
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}