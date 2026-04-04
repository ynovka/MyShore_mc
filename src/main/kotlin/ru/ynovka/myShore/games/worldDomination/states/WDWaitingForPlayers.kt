package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.hub.Hub
import ru.ynovka.myShore.text.ActionBarController


// Ожидание игроков (нужно хотя бы 12)
object WDWaitingForPlayers : GameState<WDPlayer> {
    override fun onEnter(game: Game<WDPlayer>) {
        game.gamePlayers.map(WDPlayer::player).forEach {
            it.teleportAsync(Hub.spawn)
            it.inventory.clear()
        }
        // action bar "Ожидание игроков..." с анимацией
    }

    override fun onExit(game: Game<WDPlayer>) {
        game.gamePlayers.map(WDPlayer::player).forEach {
            ActionBarController.clear(it)
        }
    }

    override fun onPlayerJoin(game: Game<WDPlayer>, player: WDPlayer) {
        val pp = player.player
        pp.teleportAsync(Hub.spawn)
        pp.inventory.clear()

        if (game.gamePlayers.size >= WDGame.MIN_PLAYERS) {
            inst.server.scheduler.runTaskLater(inst, Runnable {
                // todo пишем всем игрокам отсчёт до начала в actionbar + тем кто только зашёл в лобби
                if (game.fsm.current != WDWaitingForPlayers) return@Runnable
                if (game.gamePlayers.size >= WDGame.MIN_PLAYERS) game.fsm.transitionTo(WDDistributionPlayers)
            }, 10 * 20L) // todo заменить на 60 * 20L - 1 минута до начала
        }
    }
}