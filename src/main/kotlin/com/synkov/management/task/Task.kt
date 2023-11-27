package com.synkov.management.task

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

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
