package com.synkov.management.task

import com.synkov.management.offsetToZone
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

@Component
class TodoistClient(
    private val properties: TodoistClientProperties
) {
    private val webClient = WebClient.builder()
        .baseUrl(properties.host)
        .defaultHeaders { it.setBearerAuth(properties.apiToken) }
        .build()

    fun findNotSynchronizedTasks(): Flux<Task> {
        return webClient.get()
            .uri {
                it.path("/tasks")
                    .queryParam("filter", "{filter}")
                    .build("due after:yesterday&!@SYNCHRONIZED&!no date&!no time")
            }
            .retrieve()
            .onStatus({ it.is5xxServerError }) {
                Mono.error(InternalServerError("Status code: ${it.statusCode().value()}"))
            }
            .bodyToFlux(Task::class.java)
            .map { mapAccordingToTimezone(it) }
            .retryWhen(
                Retry.backoff(3, Duration.ofSeconds(2))
                    .filter { it is InternalServerError }
                    .onRetryExhaustedThrow { _, retrySignal -> retrySignal.failure() }
            )
    }

    fun findTask(id: String): Mono<Task> {
        return webClient.get()
            .uri {
                it.path("/tasks/{id}")
                    .build(id)
            }
            .retrieve()
            .onStatus({ it.value() == 404 }) {
                Mono.error(TaskNotExistInTodoistException("Task $id doesn't exist in todoist"))
            }
            .onStatus({ it.is5xxServerError }) {
                Mono.error(InternalServerError("Status code: ${it.statusCode().value()}"))
            }
            .bodyToMono(Task::class.java)
            .map { mapAccordingToTimezone(it) }
            .retryWhen(
                Retry.backoff(3, Duration.ofSeconds(2))
                    .filter { it is InternalServerError }
                    .onRetryExhaustedThrow { _, retrySignal -> retrySignal.failure() }
            )
    }

    fun updateTask(id: String, request: UpdateTaskRequest): Mono<Task> {
        return webClient.post()
            .uri("/tasks/$id")
            .bodyValue(request)
            .retrieve()
            .onStatus({ it.is5xxServerError }) {
                Mono.error(InternalServerError("Status code: ${it.statusCode().value()}"))
            }
            .bodyToMono(Task::class.java)
            .map { mapAccordingToTimezone(it) }
            .retryWhen(
                Retry.backoff(3, Duration.ofSeconds(2))
                    .filter { it is InternalServerError }
                    .onRetryExhaustedThrow { _, retrySignal -> retrySignal.failure() }
            )
    }

    private fun mapAccordingToTimezone(task: Task): Task {
        return task.due?.timezone
            ?.let { zone ->
                task.copy(due = task.due.copy(timezone = null, datetime = task.due.datetime!!.offsetToZone(zone)))
            } ?: task
    }

    class InternalServerError(override val message: String) : RuntimeException()
}

data class UpdateTaskRequest(
    val labels: List<TaskLabel>
)

class TaskNotExistInTodoistException(override val message: String) : RuntimeException()
