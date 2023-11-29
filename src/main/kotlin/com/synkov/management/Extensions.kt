package com.synkov.management

import java.time.LocalDateTime
import java.time.ZoneId

fun LocalDateTime.offsetToZone(zone: String) : LocalDateTime {
    return this.atZone(ZoneId.of("UTC"))
        .withZoneSameInstant(ZoneId.of(zone))
        .toLocalDateTime()
}

fun <T> List<T>.exactlyEquals(list: List<T>): Boolean {
    return size == list.size && containsAll(list)
}
