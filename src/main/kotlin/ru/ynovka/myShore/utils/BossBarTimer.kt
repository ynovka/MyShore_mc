package ru.ynovka.myShore.utils

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.text.ComponentDecorator
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class BossBarTimer(
    color: BossBar.Color = BossBar.Color.BLUE,
    overlay: BossBar.Overlay = BossBar.Overlay.PROGRESS,
) {
    val bar: BossBar = BossBar.bossBar(
        Component.empty(),
        0.0f,
        color,
        overlay
    )

    val barTimer: BossBar = BossBar.bossBar(
        Component.empty(),
        1.0f,
        color,
        overlay
    )

    private val players = ConcurrentHashMap.newKeySet<UUID>()

    private var task: ScheduledTask? = null

    private val running = AtomicBoolean(false)

    private val endMs = AtomicLong(0L)
    private val totalMs = AtomicLong(0L)

    fun addPlayer(player: Player) {
        players.add(player.uniqueId)

        player.showBossBar(bar)
        player.showBossBar(barTimer)
    }

    fun removePlayer(player: Player) {
        players.remove(player.uniqueId)

        player.hideBossBar(bar)
        player.hideBossBar(barTimer)
    }

    /** Добавляет время к активному таймеру */
    fun addTime(seconds: Int) {
        if (seconds <= 0 || !running.get()) return

        val addedMs = seconds * 1000L

        endMs.addAndGet(addedMs)
        totalMs.addAndGet(addedMs)
    }

    private fun cancelTaskOnly() {
        running.set(false)
        task?.cancel()
        task = null
    }

    fun start(
        totalSeconds: Int,
        isActive: () -> Boolean,
        onFinish: () -> Unit,
        onCancel: () -> Unit = {},
    ) {
        cancelTaskOnly()

        val nowMs = System.currentTimeMillis()
        val durationMs = totalSeconds * 1000L

        endMs.set(nowMs + durationMs)
        totalMs.set(durationMs)
        running.set(true)

        task = inst.server.asyncScheduler.runAtFixedRate(inst, { scheduledTask ->
            if (!running.get()) {
                scheduledTask.cancel()
                return@runAtFixedRate
            }

            val currentEndMs = endMs.get()
            val currentTotalMs = totalMs.get()

            val remainingMs = (currentEndMs - System.currentTimeMillis()).coerceAtLeast(0L)
            val remainingSeconds = ((remainingMs + 999L) / 1000L).toInt()

            val progress = if (currentTotalMs <= 0L) {
                0f
            } else {
                (remainingMs.toDouble() / currentTotalMs)
                    .toFloat()
                    .coerceIn(0f, 1f)
            }

            inst.server.scheduler.runTask(inst, Runnable {
                if (!running.get()) return@Runnable

                if (!isActive()) {
                    running.set(false)
                    scheduledTask.cancel()
                    hideBars()
                    onCancel()
                    return@Runnable
                }

                barTimer.name(
                    ComponentDecorator.addBackground(
                        Component.text(remainingSeconds)
                    )
                )

                barTimer.progress(progress)

                if (remainingMs <= 0L) {
                    running.set(false)
                    scheduledTask.cancel()
                    hideBars()
                    onFinish()
                }
            })
        }, 0L, 1L, TimeUnit.SECONDS)
    }

    fun stop() {
        cancelTaskOnly()

        inst.server.scheduler.runTask(inst, Runnable {
            hideBars()
        })
    }

    private fun hideBars() {
        players.toList().forEach { uuid ->
            val player = inst.server.getPlayer(uuid) ?: return@forEach

            player.hideBossBar(bar)
            player.hideBossBar(barTimer)
        }
    }
}