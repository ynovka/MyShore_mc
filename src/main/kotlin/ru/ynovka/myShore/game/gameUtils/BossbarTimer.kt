package ru.ynovka.myShore.game.gameUtils

import ru.ynovka.myShore.game.GamePlayer.Companion.withOnlinePlayers
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import io.papermc.paper.connection.DisconnectionReason.game
import ru.ynovka.myShore.MyShore.Companion.scheduler
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import ru.ynovka.myShore.MyShore.Companion.inst
import java.util.concurrent.ConcurrentHashMap
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.minecraft.world.level.levelgen.SurfaceRules.state
import ru.ynovka.myShore.game.GamePlayer
import ru.ynovka.myShore.game.GameWorld
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.game.Game
import java.util.UUID


object BossbarTimer {

    /**
     * ```kotlin
     * startCountdownTimer(
     *     time = 30,
     *     game = game,
     *     state = this,
     *     onCompletion = { g, s ->
     *         g.fsm.transition(PillarsInProgress(g))
     *     }
     * )
     * ```
     *
     * @param time время таймера в секундах
     * @param onCompletion задача, которая будет выполнена при завершении таймера
     */
    fun <P : GamePlayer,
            W : GameWorld,
            G : Game<P, W>,
            S : GameState<P, W, G>
            > startCountdownTimer(
        time: Int,
        game: G,
        state: S,
        onCompletion: ((game: G, state: S) -> Unit)? = null
    ): BossbarTimerHandle {
        val timeLeft = AtomicInteger(time)
        val maxTime = AtomicInteger(time.coerceAtLeast(1))
        val cancelled = AtomicBoolean(false)

        val handle = BossbarTimerHandle(
            timeLeft = timeLeft,
            maxTime = maxTime,
            cancelled = cancelled
        )

        val bar = BossBar.bossBar(Component.empty(), 0.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
        val barTimer = BossBar.bossBar(Component.empty(), 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)

        val viewers = ConcurrentHashMap.newKeySet<UUID>()

        fun hideBars(playerId: UUID) {
            val player = inst.server.getPlayer(playerId) ?: return
            player.hideBossBar(bar)
            player.hideBossBar(barTimer)
        }

        fun hideAllBars() {
            viewers.toList().forEach(::hideBars)
            viewers.clear()
        }

        fun syncViewers() {
            game.gamePlayers.withOnlinePlayers { currentPlayers ->
                val currentIds = currentPlayers.mapTo(mutableSetOf()) { it.uniqueId }

                viewers.filterNot { it in currentIds }.toSet().also { removed ->
                    removed.forEach(::hideBars)
                    viewers.removeAll(removed)
                }

                currentPlayers.forEach { player ->
                    val uuid = player.uniqueId
                    if (viewers.add(uuid)) {
                        player.showBossBar(bar)
                        player.showBossBar(barTimer)
                    }
                }
            }
        }

        val task = scheduler.schedule {
            val currentTime = timeLeft.getAndDecrement().coerceAtLeast(0)
            val progress = if (maxTime.get() <= 0) 0f
            else (currentTime.toFloat() / maxTime.get().toFloat()).coerceIn(0f, 1f)

            syncViewers()

            barTimer.name(Component.text(currentTime))
            barTimer.progress(progress)
        }
            .global()
            .repeatWhile { game.fsm.current === state && timeLeft.get() > 0 && !cancelled.get() }
            .repeatEvery(20L, Clock.TICKS)

        task.completion().whenComplete { _, throwable ->
            hideAllBars()

            if (throwable != null) {
                throwable.printStackTrace()
                return@whenComplete
            }

            if (game.fsm.current === state && timeLeft.get() <= 0 && !cancelled.get() && onCompletion != null) {
                scheduler.schedule {
                    onCompletion(game, state)
                }.global().once()
            }
        }

        return handle
    }

    class BossbarTimerHandle(
        private val timeLeft: AtomicInteger,
        private val maxTime: AtomicInteger,
        private val cancelled: AtomicBoolean,
    ) {
        fun addTime(seconds: Int) {
            if (seconds <= 0) return
            timeLeft.addAndGet(seconds)
            maxTime.addAndGet(seconds)
        }

        fun removeTime(seconds: Int) {
            if (seconds <= 0) return
            val newTime = timeLeft.addAndGet(-seconds)
            if (newTime < 0) timeLeft.set(0)
        }

        fun setTime(seconds: Int) {
            val safeSeconds = seconds.coerceAtLeast(0)
            timeLeft.set(safeSeconds)
            maxTime.set(safeSeconds.coerceAtLeast(1))
        }

        fun remaining(): Int = timeLeft.get().coerceAtLeast(0)

        fun cancel() {
            cancelled.set(true)
            timeLeft.set(0)
        }
    }
}