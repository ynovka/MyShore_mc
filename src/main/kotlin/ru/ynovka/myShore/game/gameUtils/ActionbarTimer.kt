package ru.ynovka.myShore.game.gameUtils

import ru.ynovka.myShore.game.GamePlayer.Companion.forEachOnlinePlayer
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.MyShore.Companion.scheduler
import java.util.concurrent.atomic.AtomicBoolean
import ru.ynovka.myShore.text.withBackground
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.game.GamePlayer
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.game.GameWorld
import ru.ynovka.myShore.game.Game
import org.bukkit.Sound


object ActionbarTimer {

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
     * @param time время таймера в секундах
     * @param componentKey ключ перевода сообщения таймера
     * @param onCompletion задача, которая будет выполнена при завершении таймера
     */
    fun <
            P : GamePlayer,
            W : GameWorld,
            G : Game<P, W>,
            S : GameState<P, W, G>
            > startCountdownTimer(
        time: Int,
        game: G,
        state: S,
        componentKey: String = "bar.myshore.start_in",
        playSound: Boolean = true,
        onCompletion: ((game: G, state: S) -> Unit)? = null,
    ): ActionbarTimerHandler {
        var timeLeft = time
        val handler = ActionbarTimerHandler()

        val task = scheduler.schedule {
            val currentTime = timeLeft--

            game.getPlayers().forEachOnlinePlayer { player ->
                player.sendActionBar(
                    Component.translatable(componentKey, Component.text(currentTime))
                        .withBackground(player)
                )

                if (playSound) {
                    player.playSound(player, Sound.BLOCK_COPPER_BULB_TURN_ON, 0.5f, 2f)
                }
            }
        }
            .global()
            .repeatWhile { game.fsm.current === state && timeLeft > 0 && !handler.isCancelled }
            .repeatEvery(20L, Clock.TICKS)

        task.completion().thenRun {
            if (game.fsm.current === state && !handler.isCancelled && onCompletion != null) {
                scheduler.schedule {
                    onCompletion(game, state)
                }.global().once()
            }
        }

        return handler
    }

    data class ActionbarTimerHandler(
        private val cancelled: AtomicBoolean = AtomicBoolean(false)
    ) {
        val isCancelled: Boolean
            get() = cancelled.get()

        fun cancel() {
            cancelled.set(true)
        }
    }
}