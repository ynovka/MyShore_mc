package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.game.gameUtils.ActionbarWaitingFor
import ru.ynovka.myShore.game.pillars.PillarsWorldManager
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.utils.canMove
import ru.ynovka.myShore.hub.HubItems
import org.bukkit.GameMode
import org.bukkit.Sound


class PillarsWaitingForPlayers(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {

    override fun onEnterState() {
        ActionbarWaitingFor.startRendering(
            game = game,
            state = this,
            componentKey = "bar.myshore.waiting_for_players"
        )

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