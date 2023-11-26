package com.synkov.management.notification

import java.time.LocalDateTime
import java.util.*

data class Notification(
    val id: UUID,
    val taskId: String,
    val title: String,
    val description: String,
    val eventAt: LocalDateTime,
    val notifyAt: LocalDateTime,
    val completed: Boolean
)
