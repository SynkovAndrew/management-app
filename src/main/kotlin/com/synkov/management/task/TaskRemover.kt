package com.synkov.management.task

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

@Component
@DependsOn("liquibase")
class TaskRemover(
    private val todoistClient: TodoistClient,
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
            .doOnSubscribe { log.info("Task remover started") }
            .doFinally { log.info("Task remover stopped") }
            .concatMap {
                taskRepository.findNotCompletedIds()
                    .flatMap { taskId ->
                        todoistClient.findTask(taskId)
                            .filter { it.isSynchronized() }
                            .map { it.id }
                            .onErrorResume(TaskNotExistInTodoistException::class.java) {
                                deleteTaskAndNotifications(taskId)
                                    .doOnSuccess { log.info("Task(id={}) removed", taskId) }
                            }
                            .`as`(transactionalOperator::transactional)
                            .doOnError { error -> log.error("Task remover failed", error) }
                    }
                    .onErrorResume { Mono.empty() }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
        disposable.add(subscription)
    }

    private fun deleteTaskAndNotifications(taskId: String): Mono<String> {
        return taskRepository.delete(taskId)
            .then(notificationRepository.deleteAllByTaskId(taskId))
            .thenReturn(taskId)
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}