package ru.ynovka.myShore.games.pillars.states

import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.pillars.PillarsGame
import ru.ynovka.myShore.games.pillars.PillarsPlayer
import ru.ynovka.myShore.games.pillars.PillarsWorld
import ru.ynovka.myShore.text.ComponentDecorator
import ru.ynovka.myShore.text.actionBar.clearActionBar
import ru.ynovka.myShore.text.actionBar.sendPermanentActionBar
import ru.ynovka.myShore.utils.canMove


class PillarsWaitingForPlayers(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {

    override fun onEnterState() {
        game.gamePlayers.forEach { tagPlayer ->
            tagPlayer.player.setupForWaiting(game)
            tagPlayer.player.playSound(tagPlayer.player.location, Sound.BLOCK_COPPER_BULB_TURN_OFF, 0.5f, 2f)
        }
    }

    override fun onPlayerJoin(gamePlayer: PillarsPlayer) {
        gamePlayer.player.setupForWaiting(game)

        if (game.gamePlayers.size >= 2) {
            // todo game.fsm.transitionTo(TagVoting(game))
        }
    }

    companion object {
        fun Player.setupForWaiting(game: PillarsGame) {
            // game.map.teleport(this, game)
            gameMode = GameMode.ADVENTURE
            // todo хотбар - выбор лобби, выход в хаб
            clearActivePotionEffects()
            canMove(true)

            object : BukkitRunnable() {
                val frames = arrayOf(".", "..", "...")
                var frame = 0

                override fun run() {
                    if (game.fsm.current !is PillarsWaitingForPlayers) {
                        cancel()
                        return
                    }
                    if (game.gamePlayers.firstOrNull { it.playerId == this@setupForWaiting.uniqueId } == null) {
                        clearActionBar()
                        cancel()
                        return
                    }

                    sendPermanentActionBar(
                        ComponentDecorator.addBackground(
                            Component.translatable("bar.myshore.tag.waiting_for_players")
                                .append(Component.text(frames[frame])),
                            this@setupForWaiting
                        )
                    )

                    frame++
                    if (frame == frames.size) frame = 0
                }
            }.runTaskTimer(inst, 0L, 10L)
        }
    }
}