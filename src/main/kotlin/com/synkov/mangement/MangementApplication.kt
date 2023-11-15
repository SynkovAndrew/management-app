package com.synkov.mangement

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MangementApplication

fun main(args: Array<String>) {
    runApplication<MangementApplication>(*args)
}
