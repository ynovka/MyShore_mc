package ru.ynovka.myShore.utils

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import ru.ynovka.myShore.MyShore.Companion.inst
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong


class AsyncTimer {

    private var task: ScheduledTask? = null

    private val running = AtomicBoolean(false)

    private val endMs = AtomicLong(0L)
    private val totalMs = AtomicLong(0L)

    fun addTime(seconds: Int) {
        if (seconds <= 0) return
        if (!running.get()) return

        val addedMs = seconds * 1000L

        endMs.addAndGet(addedMs)
        totalMs.addAndGet(addedMs)
    }

    fun cancel() {
        if (!running.getAndSet(false)) return

        task?.cancel()
        task = null
    }

    fun start(
        totalSeconds: Int,
        isActive: () -> Boolean = { true },
        onCancel: () -> Unit = {},
        onFinish: () -> Unit,
    ) {
        cancel()

        val durationMs = totalSeconds * 1000L
        val nowMs = System.currentTimeMillis()

        endMs.set(nowMs + durationMs)
        totalMs.set(durationMs)
        running.set(true)

        task = inst.server.asyncScheduler.runAtFixedRate(inst, { scheduledTask ->
            if (!running.get()) {
                scheduledTask.cancel()
                return@runAtFixedRate
            }

            val remainingMs = (endMs.get() - System.currentTimeMillis()).coerceAtLeast(0L)

            if (!isActive()) {
                if (running.getAndSet(false)) {
                    scheduledTask.cancel()
                    task = null

                    inst.server.scheduler.runTask(inst, Runnable {
                        onCancel()
                    })
                }

                return@runAtFixedRate
            }

            if (remainingMs <= 0L) {
                if (running.getAndSet(false)) {
                    scheduledTask.cancel()
                    task = null

                    inst.server.scheduler.runTask(inst, Runnable {
                        onFinish()
                    })
                }
            }
        }, 0L, 1L, TimeUnit.SECONDS)
    }

    fun isRunning(): Boolean {
        return running.get()
    }

    fun getRemainingSeconds(): Int {
        if (!running.get()) return 0

        return ((endMs.get() - System.currentTimeMillis() + 999L) / 1000L)
            .toInt()
            .coerceAtLeast(0)
    }

    fun getProgress(): Float {
        val currentTotalMs = totalMs.get()
        if (currentTotalMs <= 0L) return 0f

        val remainingMs = (endMs.get() - System.currentTimeMillis()).coerceAtLeast(0L)

        return (remainingMs.toDouble() / currentTotalMs)
            .toFloat()
            .coerceIn(0f, 1f)
    }
}