package main.utils.task

import java.util.concurrent.Executors


object TaskManager {

    private val excutors = Executors.newSingleThreadExecutor()


    fun execute(runnable: Runnable) {
        excutors.submit(runnable)
    }
}