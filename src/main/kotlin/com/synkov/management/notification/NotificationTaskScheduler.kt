package com.synkov.management.notification

import com.synkov.management.todoist.TodoistClient
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.Disposables
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.*

@Component
class NotificationTaskScheduler(
    private val todoistClient: TodoistClient,
    private val notificationTaskRepository: NotificationTaskRepository
) {
    private val disposable = Disposables.composite()

    @PreDestroy
    fun destroy() {
        disposable.dispose()
    }

    @PostConstruct
    fun start() {
        val subscription = Flux.interval(Duration.ofSeconds(5))
            .doOnSubscribe { log.info("Notification task scheduler started") }
            .doFinally { log.info("Notification task scheduler stopped") }
            .concatMap {
                todoistClient.findTasks()
                    .concatMap { todoistTask ->
                        notificationTaskRepository.countByExternalId(todoistTask.id)
                            .map { it to todoistTask }
                    }
                    .filter { (count, _) -> count == 0L }
                    .filter { (_, todoistTask) -> todoistTask.due?.datetime != null }
                    .map { (_, todoistTask) ->
                        NotificationTask(
                            id = UUID.randomUUID(),
                            externalId = todoistTask.id,
                            content = todoistTask.content,
                            timestamp = todoistTask.due!!.datetime!!,
                            completed = false
                        )
                    }
                    .concatMap { notificationTaskRepository.save(it) }
                    .doOnNext { log.info("{} scheduled", it) }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
        disposable.add(subscription)
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}