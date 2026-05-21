package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.game.gameUtils.ActionbarWaitingFor
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.hub.HubItems
import org.bukkit.entity.Player
import org.bukkit.GameMode
import org.bukkit.Sound
import ru.ynovka.myShore.utils.restrictToBlock


class PillarsWaitingForPlayers(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {

    override fun onEnterState() {
        ActionbarWaitingFor.startRendering(
            game = game,
            state = this,
            componentKey = "bar.myshore.waiting_for_players"
        )

        game.gamePlayers.forEach { pPlayer ->
            val player = pPlayer.player
            scheduler.schedule {
                setupForWaiting(player, pPlayer, game)
                player.playSound(player.location, Sound.BLOCK_COPPER_BULB_TURN_OFF, 0.5f, 2f)
            }.entity(player).once()
        }
    }

    override fun onPlayerJoin(gamePlayer: PillarsPlayer) {
        if (game.gamePlayers.size >= 2) {
            game.fsm.transitionTo(PillarsCountdown(game))
            return
        }

        val player = gamePlayer.player
        scheduler.schedule {
            setupForWaiting(player, gamePlayer, game)
        }.entity(player).once()
    }

    private fun setupForWaiting(
        player: Player,
        pPlayer: PillarsPlayer,
        game: PillarsGame
    ) {
        println("setupForWaiting")
        game.gameWorld.spawnPlayer(game, pPlayer).thenRun {
            player.restrictToBlock(true)
        }
        player.gameMode = GameMode.ADVENTURE
        player.clearActivePotionEffects()
        player.inventory.clear()
        player.inventory.setItem(8, HubItems.hubTeleport.getStack(null))
    }
}