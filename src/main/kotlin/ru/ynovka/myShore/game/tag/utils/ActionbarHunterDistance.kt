package ru.ynovka.myShore.game.tag.utils

import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.tag.TagPlayerRoles
import ru.ynovka.myShore.text.ComponentDecorator
import ru.ynovka.myShore.game.tag.TagPlayer
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.game.GameWorld
import ru.ynovka.myShore.game.Game
import kotlin.math.roundToInt


object ActionbarHunterDistance {

    fun <
            W : GameWorld,
            G : Game<TagPlayer, W>,
            S : GameState<TagPlayer, W, G>
    > startRendering(
        game: G,
        state: S,
    ) {
        scheduler.schedule {
            val hunter = game.gamePlayers.firstOrNull { it.role == TagPlayerRoles.HUNTER }?.player
                ?: return@schedule

            scheduler.schedule {
                val hunterLoc = hunter.location.clone()

                game.gamePlayers
                    .filter { it.role == TagPlayerRoles.VICTIM }
                    .asPlayers().forEach { player ->
                        scheduler.schedule {
                            val victimLoc = player.location
                            if (victimLoc.world.uid != hunterLoc.world.uid) return@schedule
                            val distance = ((victimLoc.distance(hunterLoc) * 10).roundToInt() / 10.0)
                            player.sendActionBar(
                                ComponentDecorator.addBackground(
                                    Component.translatable(
                                        "bar.myshore.tag.distance_to_hunter",
                                        Component.text(distance)
                                    ),
                                    player
                                )
                            )
                        }.entity(player).once()
                    }
            }.entity(hunter).once()
        }
            .global()
            .repeatWhile { game.fsm.current === state }
            .repeatEvery(1L, Clock.TICKS)
    }

}