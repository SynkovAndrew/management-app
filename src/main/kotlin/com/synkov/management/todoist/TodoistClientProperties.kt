package com.synkov.management.todoist

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("todoist")
data class TodoistClientProperties(
    val apiToken: String,
    val host: String
)
