package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.game.gameUtils.ActionbarWaitingFor
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.utils.restrictToBlock
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.hub.HubItems
import org.bukkit.entity.Player
import org.bukkit.GameMode
import org.bukkit.Sound


class PillarsWaitingForPlayers(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {

    override fun onEnterState() {
        ActionbarWaitingFor.startRendering(
            game = game,
            state = this,
            componentKey = "bar.myshore.waiting_for_players"
        )

        game.activePlayers.forEach { pPlayer ->
            pPlayer.withOnlinePlayer { player ->
                setupForWaiting(player, pPlayer, game)
            }
        }
    }

    override fun onPlayerJoin(gamePlayer: PillarsPlayer) {
        if (game.activePlayers.size >= 2) {
            game.fsm.transitionTo(PillarsCountdown(game))
            return
        }

        gamePlayer.withOnlinePlayer { player ->
            setupForWaiting(player, gamePlayer, game)
        }
    }

    private fun setupForWaiting(
        player: Player,
        pPlayer: PillarsPlayer,
        game: PillarsGame
    ) {
        game.gameWorld.spawnPlayer(game, pPlayer).thenRun {
            player.restrictToBlock(true)
        }
        scheduler.schedule {
            player.gameMode = GameMode.ADVENTURE
            player.clearActivePotionEffects()
            player.inventory.clear()
            player.inventory.setItem(8, HubItems.hubTeleport.getStack(null))
            player.playSound(player.location, Sound.BLOCK_COPPER_BULB_TURN_OFF, 0.5f, 2f)
        }.entity(player).once()
    }
}
