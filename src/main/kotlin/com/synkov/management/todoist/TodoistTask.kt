package com.synkov.management.todoist

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.TimeZone

@JsonIgnoreProperties(ignoreUnknown = true)
data class TodoistTask(
    val id: String,
    val isCompleted: Boolean,
    val content: String,
    val description: String,
    val due: Due?,
    val url: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Due(
    val date: LocalDate,
    val isRecurring: Boolean,
    val datetime: LocalDateTime?,
    val string: String,
    val timezone: TimeZone?
)
