package com.synkov.management.todoist

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Component
class TodoistClient(
    private val properties: TodoistClientProperties
) {
    private val webClient = WebClient.builder()
        .baseUrl(properties.host)
        .defaultHeaders { it.setBearerAuth(properties.apiToken) }
        .build()

    fun findTasks(): Flux<TodoistTask> {
        return webClient.get()
            .uri("/tasks?filter=due after:yesterday")
            .retrieve()
            .bodyToFlux(TodoistTask::class.java)
    }
}
