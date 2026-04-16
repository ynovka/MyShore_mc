package ru.ynovka.myShore.games.worldDomination.states

import org.bukkit.scheduler.BukkitTask
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.hub.Hub
import ru.ynovka.myShore.text.actionBar.ActionBar


// Ожидание игроков (нужно хотя бы 12)
class WDWaitingForPlayers(game: WDGame) : GameState<WDPlayer, WDGame>(game) {
    private var startTask: BukkitTask? = null

    override fun onEnterState() {
        game.gamePlayers.map(WDPlayer::player).forEach {
            it.teleportAsync(Hub.spawn)
            it.inventory.clear()
        }
        // if (task == null) action bar "Ожидание игроков..." с анимацией
    }

    override fun onExitState() {
        startTask?.cancel()
        startTask = null
        game.gamePlayers.map(WDPlayer::player).forEach {
            ActionBar.clear(it)
        }
    }

    override fun onPlayerJoin(gamePlayer: WDPlayer) {
        val player = gamePlayer.player
        player.teleportAsync(WDGame.hubLoc)
        player.inventory.clear()

        if (startTask != null) return
        if (game.gamePlayers.size >= WDGame.MIN_PLAYERS) {
            startTask = inst.server.scheduler.runTaskLater(inst, Runnable {
                // todo пишем всем игрокам отсчёт до начала в actionbar + тем кто только зашёл в лобби
                if (game.gamePlayers.size >= WDGame.MIN_PLAYERS) game.fsm.transitionTo(WDDistributionPlayers(game))
            }, 10 * 20L) // todo заменить на 60 * 20L - 1 минута до начала
        }
    }
}