package com.synkov.management.task

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.synkov.management.exactlyEquals
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.function.BiPredicate

@JsonIgnoreProperties(ignoreUnknown = true)
data class Task(
    val id: String,
    val isCompleted: Boolean,
    val content: String,
    val description: String,
    val labels: List<TaskLabel> = listOf(),
    val due: Due?,
    val url: String,
    val isProcessed: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
) {
    fun isSynchronized() = labels.contains(TaskLabel.SYNCHRONIZED)

    fun synchronize(): Task {
        return copy(labels = labels + TaskLabel.SYNCHRONIZED)
    }

    fun process(): Task {
        return copy(isProcessed = true)
    }

    fun update(
        content: String,
        description: String,
        due: Due?,
        labels: List<TaskLabel>
    ): Task {
        return copy(
            content = content,
            description = description,
            due = due?.date?.let {
                this.due?.copy(
                    date = it,
                    isRecurring = due.isRecurring,
                    datetime = due.datetime
                )
            },
            labels = labels
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Due(
    val date: LocalDate,
    @JsonProperty("is_recurring")
    val isRecurring: Boolean,
    val datetime: LocalDateTime?,
    val string: String,
    val timezone: String?
)

enum class TaskLabel(val remindAt: (LocalDateTime) -> LocalDateTime) {
    SYNCHRONIZED({ throw IllegalArgumentException("SYNCHRONIZED value doesn't have remindAt function") }),
    REMIND_AT_EVENT_TIME({ it }),
    REMIND_5_MIN_BEFORE({ it.minusMinutes(5) }),
    REMIND_30_MIN_BEFORE({ it.minusMinutes(30) }),
    REMIND_2_HOURS_BEFORE({ it.minusHours(2) }),
    REMIND_4_HOURS_BEFORE({ it.minusHours(4) }),
    REMIND_6_HOURS_BEFORE({ it.minusHours(6) }),
    REMIND_1_DAY_BEFORE_IN_EVENING({ LocalDateTime.of(it.minusDays(1).toLocalDate(), evening) }),
    REMIND_1_DAY_BEFORE_IN_MIDDAY({ LocalDateTime.of(it.minusDays(1).toLocalDate(), midday) }),
    REMIND_1_DAY_BEFORE_IN_MORNING({ LocalDateTime.of(it.minusDays(1).toLocalDate(), morning) }),
    REMIND_2_DAY_BEFORE_IN_EVENING({ LocalDateTime.of(it.minusDays(2).toLocalDate(), evening) }),
    REMIND_2_DAY_BEFORE_IN_MIDDAY({ LocalDateTime.of(it.minusDays(2).toLocalDate(), midday) }),
    REMIND_2_DAY_BEFORE_IN_MORNING({ LocalDateTime.of(it.minusDays(2).toLocalDate(), morning) }),
    REMIND_3_DAY_BEFORE_IN_EVENING({ LocalDateTime.of(it.minusDays(3).toLocalDate(), evening) }),
    REMIND_3_DAY_BEFORE_IN_MIDDAY({ LocalDateTime.of(it.minusDays(3).toLocalDate(), midday) }),
    REMIND_3_DAY_BEFORE_IN_MORNING({ LocalDateTime.of(it.minusDays(3).toLocalDate(), morning) }),
    REMIND_1_WEEK_BEFORE_IN_EVENING({ LocalDateTime.of(it.minusWeeks(1).toLocalDate(), evening) }),
    REMIND_1_WEEK_BEFORE_IN_MIDDAY({ LocalDateTime.of(it.minusWeeks(1).toLocalDate(), midday) }),
    REMIND_1_WEEK_BEFORE_IN_MORNING({ LocalDateTime.of(it.minusWeeks(1).toLocalDate(), morning) });
}

private val morning: LocalTime = LocalTime.of(9, 0)
private val midday: LocalTime = LocalTime.of(13, 0)
private val evening: LocalTime = LocalTime.of(19, 0)

object TaskEqualityTester : BiPredicate<Task, Task> {
    override fun test(t: Task, u: Task): Boolean {
        return (t.content == u.content) &&
                (t.description == u.description) &&
                (t.due?.date == u.due?.date) &&
                (t.due?.isRecurring == u.due?.isRecurring) &&
                (t.due?.datetime == u.due?.datetime) &&
                (t.labels.exactlyEquals(u.labels))
    }
}

object TaskNotificationEqualityTester : BiPredicate<Task, Task> {
    override fun test(t: Task, u: Task): Boolean {
        return (t.due?.date == u.due?.date) &&
                (t.due?.datetime == u.due?.datetime) &&
                (t.labels.exactlyEquals(u.labels))
    }
}
