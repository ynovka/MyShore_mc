package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.hub.Hub


// Ожидание игроков (нужно хотя бы 2)
object WDWaitingForPlayers : GameState<WDPlayer> {
    override fun onEnter(game: Game<WDPlayer>) {
        // action bar "Ожидание игроков..." с анимацией
    }

    override fun onExit(game: Game<WDPlayer>) {
        // action bar clear
    }

    override fun onPlayerJoin(game: Game<WDPlayer>, player: WDPlayer) {
        player.player.teleportAsync(Hub.spawn)

        if (game.gamePlayers.size >= WDGame.MIN_PLAYERS) game.fsm.transitionTo(WDDistributionPlayers)
    }
}