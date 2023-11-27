package com.synkov.management.task

import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class TaskRepository(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
) {

    fun findAllIds(): Flux<String> {
        return r2dbcEntityTemplate.databaseClient
            .sql("SELECT id FROM task")
            .map { row, _ -> row.get("id", String::class.java)!! }
            .all()
    }

    fun findNextNotProcessed(): Mono<Task> {
        return r2dbcEntityTemplate.databaseClient
            .sql(
                """SELECT * FROM task t
                   LEFT JOIN task_label tl ON tl.task_id = t.id
                   WHERE t.is_processed = false
                   ORDER BY t.created_at
                   LIMIT 1
                   FOR UPDATE OF t SKIP LOCKED""".trimMargin()
            )
            .map { row, meta -> r2dbcEntityTemplate.converter.read(TaskReadEntity::class.java, row, meta) }
            .all()
            .collectList()
            .filter { it.isNotEmpty() }
            .map { toDomain(it) }
    }

    fun create(task: Task): Mono<Task> {
        return Mono.just(task)
            .flatMap {
                Mono.zip(
                    r2dbcEntityTemplate.insert(toEntity(it)),
                    Flux.fromIterable(toLabelEntities(it.id, it.labels))
                        .flatMap { labelEntity -> r2dbcEntityTemplate.insert(labelEntity) }
                        .collectList()
                )
            }
            .thenReturn(task)
    }

    fun update(task: Task): Mono<Task> {
        return Mono.just(task)
            .flatMap {
                Mono
                    .zip(
                        r2dbcEntityTemplate.update(toEntity(it)),
                        r2dbcEntityTemplate.delete(
                            Query.query(Criteria.where("task_id").`is`(it.id)),
                            TaskLabelEntity::class.java
                        )
                    )
                    .then(
                        Flux.fromIterable(toLabelEntities(it.id, it.labels))
                            .flatMap { labelEntity -> r2dbcEntityTemplate.insert(labelEntity) }
                            .collectList()
                    )
            }
            .thenReturn(task)
    }

    fun delete(id: String): Mono<String> {
        return Mono
            .zip(
                r2dbcEntityTemplate.databaseClient
                    .sql("DELETE FROM task WHERE id = :id RETURNING id")
                    .bind("id", id)
                    .map { row, _ -> row.get("id", String::class.java)!! }
                    .one(),
                r2dbcEntityTemplate.databaseClient
                    .sql("DELETE FROM task_label WHERE task_id = :taskId RETURNING task_id")
                    .bind("taskId", id)
                    .map { row, _ -> row.get("task_id", String::class.java)!! }
                    .all()
                    .collectList()
            )
            .thenReturn(id)
    }

    private fun toDomain(entities: List<TaskReadEntity>): Task {
        return with(entities.first()) {
            Task(
                id,
                isCompleted,
                content,
                description,
                entities.mapNotNull { it.label }.map { TaskLabel.valueOf(it) },
                date?.let {
                    Due(
                        it,
                        isRecurring!!,
                        datetime,
                        string!!,
                        timezone,
                    )
                },
                url,
                isProcessed,
                createdAt
            )
        }
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
                url,
                isProcessed,
                createdAt
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
    val url: String,
    val isProcessed: Boolean,
    val createdAt: LocalDateTime
)

@Table("task_label")
data class TaskLabelEntity(
    val taskId: String,
    val label: String
)

data class TaskReadEntity(
    val id: String,
    val isCompleted: Boolean,
    val content: String,
    val description: String,
    val date: LocalDate?,
    val isRecurring: Boolean?,
    val datetime: LocalDateTime?,
    val string: String?,
    val timezone: String?,
    val url: String,
    val label: String?,
    val isProcessed: Boolean,
    val createdAt: LocalDateTime
)
