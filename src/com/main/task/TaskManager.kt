package com.main.task

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


object TaskManager {

    private val executors = Executors.newSingleThreadExecutor()
    private val scheduledExecutors = Executors.newScheduledThreadPool(1)


    fun execute(runnable: Runnable) {
        executors.submit(runnable)
    }

    fun executeDelay(runnable: Runnable, delay: Long) {
        scheduledExecutors.schedule(runnable, delay, TimeUnit.MILLISECONDS)
    }
}