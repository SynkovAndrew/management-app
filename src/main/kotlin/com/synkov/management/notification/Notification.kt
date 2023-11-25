package com.synkov.management.notification

import java.time.LocalDateTime
import java.util.*

data class Notification(
    val id: UUID,
    val externalId: String,
    val content: String,
    val timestamp: LocalDateTime,
    val completed: Boolean
)
