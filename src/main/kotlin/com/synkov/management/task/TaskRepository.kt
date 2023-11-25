package com.synkov.management.task

import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class TaskRepository(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
) {

    fun create(task: Task): Mono<Task> {
        return Mono.just(task)
            .flatMap {
                r2dbcEntityTemplate.insert(toEntity(it))
                    .then(
                        Flux.fromIterable(toLabelEntities(it.id, it.labels))
                            .flatMap { labelEntity -> r2dbcEntityTemplate.insert(labelEntity) }
                            .collectList()
                    )
            }
            .thenReturn(task)
    }

    private fun toEntity(task: Task): TaskEntity {
        return with(task) {
            TaskEntity(
                id,
                isCompleted,
                content,
                description,
                due?.date,
                due?.isRecurring,
                due?.datetime,
                due?.string,
                due?.timezone,
                url
            )
        }
    }

    private fun toLabelEntities(taskId: String, labels: List<TaskLabel>?): List<TaskLabelEntity> {
        return labels?.map {
            TaskLabelEntity(
                taskId,
                it.name
            )
        } ?: listOf()
    }
}

@Table("task")
data class TaskEntity(
    @Id
    val id: String,
    val isCompleted: Boolean,
    val content: String,
    val description: String,
    val date: LocalDate?,
    val isRecurring: Boolean?,
    val datetime: LocalDateTime?,
    val string: String?,
    val timezone: String?,
    val url: String
)

@Table("task_label")
data class TaskLabelEntity(
    val taskId: String,
    val label: String
)
