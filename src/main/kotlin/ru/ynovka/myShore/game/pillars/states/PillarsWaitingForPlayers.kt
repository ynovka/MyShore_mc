package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.text.actionBar.sendPermanentActionBar
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.game.pillars.PillarsWorldManager
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.text.ComponentDecorator
import ru.ynovka.myShore.game.GamePlayer
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.utils.canMove
import ru.ynovka.myShore.hub.HubItems
import org.bukkit.GameMode
import org.bukkit.Sound
import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers


class PillarsWaitingForPlayers(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {

    override fun onEnterState() {
        waitingForPlayersActionbar()

        game.gamePlayers.forEach { pPlayer ->
            pPlayer.setupForWaiting(game)
            pPlayer.player.playSound(pPlayer.player.location, Sound.BLOCK_COPPER_BULB_TURN_OFF, 0.5f, 2f)
        }
    }

    override fun onPlayerJoin(gamePlayer: PillarsPlayer) {
        gamePlayer.setupForWaiting(game)

        if (game.gamePlayers.size >= 2) {
            game.fsm.transitionTo(PillarsCountdown(game))
        }
    }

    private fun waitingForPlayersActionbar() {
        val frames = arrayOf(".", "..", "...")
        var frame = 0

        scheduler.schedule {
            game.gamePlayers.asPlayers().forEach { player ->
                player.sendPermanentActionBar(
                    ComponentDecorator.addBackground(
                        Component.translatable("bar.myshore.waiting_for_players")
                            .append(Component.text(frames[frame])),
                        player
                    )
                )

                frame++
                if (frame == frames.size) frame = 0
            }
        }
            .repeatWhile { game.fsm.current is PillarsWaitingForPlayers }
            .repeatEvery(10L, Clock.TICKS)
    }

    companion object {
        fun PillarsPlayer.setupForWaiting(game: PillarsGame) {
            val player = this.player
            PillarsWorldManager.spawnPlayer(game, this)
            player.gameMode = GameMode.ADVENTURE
            player.clearActivePotionEffects()
            player.canMove(false)
            player.inventory.clear()
            player.inventory.setItem(8, HubItems.hubTeleport.getStack(null))
        }
    }
}