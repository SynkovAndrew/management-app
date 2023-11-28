package com.synkov.management.task

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Objects
import java.util.function.BiPredicate

@JsonIgnoreProperties(ignoreUnknown = true)
data class Task(
    val id: String,
    val isCompleted: Boolean,
    val content: String,
    val description: String,
    val labels: List<TaskLabel>?,
    val due: Due?,
    val url: String,
    val isProcessed: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
) {
    fun synchronize(): Task {
        return copy(
            labels = labels?.let { it + TaskLabel.SYNCHRONIZED }
                ?: listOf(TaskLabel.SYNCHRONIZED),
        )
    }

    fun process(): Task {
        return copy(isProcessed = true)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Due(
    val date: LocalDate,
    val isRecurring: Boolean,
    val datetime: LocalDateTime?,
    val string: String,
    val timezone: String?
)

enum class TaskLabel {
    SYNCHRONIZED
}

object TaskEqualityTester : BiPredicate<Task, Task> {
    override fun test(t: Task, u: Task): Boolean {
        return (t.content == u.content) &&
                (t.description == u.description) &&
                (t.due?.date == u.due?.date) &&
                (t.due?.isRecurring == u.due?.isRecurring) &&
                (t.due?.datetime == u.due?.datetime)
    }
}
