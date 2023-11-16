package com.synkov.management.telegram

import org.springframework.core.env.Environment
import org.springframework.core.env.getProperty
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.db.DBContext
import org.telegram.abilitybots.api.sender.SilentSender
import java.awt.GraphicsEnvironment

@Component
class ReminderBot(environment: Environment) :
    AbilityBot(environment.getProperty("botToken"), "reminderBot") {

    override fun creatorId(): Long = 1L
}

class ResponseHandler(val silentSender: SilentSender, dbContext: DBContext) {
    private val chatStates: Map<Long, UserState> = dbContext.getMap("")
}