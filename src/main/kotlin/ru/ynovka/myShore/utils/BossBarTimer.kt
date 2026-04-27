package ru.ynovka.myShore.utils

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.text.ComponentDecorator
import java.util.concurrent.TimeUnit


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

    private var endMs = 0L
    private var totalMs = 0L

    fun addPlayer(player: Player) {
        player.showBossBar(bar)
        player.showBossBar(barTimer)
    }

    fun removePlayer(player: Player) {
        player.hideBossBar(bar)
        player.hideBossBar(barTimer)
    }

    /** Добавляет время к активному таймеру не сбрасывая прогресс резко. */
    fun addTime(seconds: Int) {
        endMs   += seconds * 1000L
        totalMs += seconds * 1000L
    }

    fun start(
        totalSeconds: Int,
        isActive: () -> Boolean,
        onFinish: () -> Unit,
    ) {
        val nowMs = System.currentTimeMillis()
        endMs   = nowMs + totalSeconds * 1000L
        totalMs = totalSeconds * 1000L

        inst.server.asyncScheduler.runAtFixedRate(inst, { task ->
            val remaining = ((endMs - System.currentTimeMillis()) / 1000L)
                .toInt()
                .coerceAtLeast(0)

            val progress = (remaining * 1000L).toDouble() / totalMs

            inst.server.scheduler.runTask(inst, Runnable {
                if (!isActive()) {
                    task.cancel()
                    stop()
                    return@Runnable
                }

                barTimer.name(ComponentDecorator.addBackground(Component.text(remaining)))
                barTimer.progress(progress.toFloat().coerceIn(0f, 1f))

                if (remaining <= 0) {
                    task.cancel()
                    stop()
                    onFinish()
                }
            })
        }, 0L, 1L, TimeUnit.SECONDS)
    }

    fun stop() {
        bar.viewers().forEach { viewer ->
            (viewer as Audience).hideBossBar(bar)
        }
        barTimer.viewers().forEach { viewer ->
            (viewer as Audience).hideBossBar(barTimer)
        }
    }
}