package com.synkov.management.task

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class Task(
    val id: String,
    val isCompleted: Boolean,
    val content: String,
    val description: String,
    val labels: List<TaskLabel>?,
    val due: Due?,
    val url: String
)

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
