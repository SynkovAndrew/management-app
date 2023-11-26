package com.synkov.management.task

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class TodoistClient(
    private val properties: TodoistClientProperties
) {
    private val webClient = WebClient.builder()
        .baseUrl(properties.host)
        .defaultHeaders { it.setBearerAuth(properties.apiToken) }
        .build()

    fun findTasks(): Flux<Task> {
        return webClient.get()
            .uri {
                it.path("/tasks")
                    .queryParam("filter", "{filter}")
                    .build("due after:yesterday&!@SYNCHRONIZED&!no date&!no time")
            }
            .retrieve()
            .bodyToFlux(Task::class.java)
    }

    fun updateTask(id: String, request: UpdateTaskRequest): Mono<Task> {
        return webClient.post()
            .uri("/tasks/$id")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Task::class.java)
    }
}

data class UpdateTaskRequest(
    val labels: List<TaskLabel>
)
