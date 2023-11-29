package com.synkov.management.task

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
class TaskSynchronizer(
    private val todoistClient: TodoistClient,
    private val taskRepository: TaskRepository,
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
            .doOnSubscribe { log.info("Task synchronizer started") }
            .doFinally { log.info("Task Synchronizer stopped") }
            .concatMap {
                todoistClient.findNotSynchronizedTasks()
                    .map { it.synchronize() }
                    .flatMap {
                        taskRepository.create(it)
                            .flatMap { task -> todoistClient.updateTask(task.id, UpdateTaskRequest(task.labels)) }
                            .`as`(transactionalOperator::transactional)
                            .doOnError { error -> log.error("Failed to synchronize $it", error) }
                            .onErrorResume { Mono.empty() }
                            .doOnSuccess { _ -> log.info("{} synchronized", it) }
                    }
                    .onErrorResume { Mono.empty() }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
        disposable.add(subscription)
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}