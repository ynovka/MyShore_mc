package ru.ynovka.myShore.game.gameUtils

import ru.ynovka.myShore.text.actionBar.sendPermanentActionBar
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.text.ComponentDecorator
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.game.GamePlayer
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.game.GameWorld
import ru.ynovka.myShore.game.Game


object ActionbarWaitingFor {

    /**
     * @param componentKey ключ перевода сообщения
     */
    fun <
            P : GamePlayer,
            W : GameWorld,
            G : Game<P, W>,
            S : GameState<P, W, G>
    > startRendering(
        game: G,
        state: S,
        componentKey: String
    ) {
        val frames = arrayOf(".", "..", "...")
        var frame = 0

        scheduler.schedule {
            frame++
            if (frame == frames.size) frame = 0

            game.gamePlayers.asPlayers().forEach { player ->
                player.sendPermanentActionBar(
                    ComponentDecorator.addBackground(
                        Component.translatable(componentKey)
                            .append(Component.text(frames[frame])),
                        player
                    )
                )
            }
        }
            .global()
            .repeatWhile { game.fsm.current === state }
            .repeatEvery(10L, Clock.TICKS)
    }

}