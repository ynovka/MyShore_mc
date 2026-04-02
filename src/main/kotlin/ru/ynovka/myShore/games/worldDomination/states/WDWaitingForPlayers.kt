package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.hub.Hub
import ru.ynovka.myShore.text.ActionBarController


// Ожидание игроков (нужно хотя бы 2)
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

        if (game.gamePlayers.size >= WDGame.MIN_PLAYERS) game.fsm.transitionTo(WDDistributionPlayers)
    }
}