package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.games.worldDomination.WDPlayer


// Ожидание игроков (нужно хотя бы 2)
object WDWaitingForPlayersState : GameState<WDPlayer> {
    override fun onEnter(game: Game<WDPlayer>) {

    }

    override fun onExit(game: Game<WDPlayer>) { }

    override fun onPlayerJoin(game: Game<WDPlayer>, player: WDPlayer) {
        if (game.gamePlayers.size >= WDGame.MIN_PLAYERS) game.fsm.transitionTo(WDDistributionPlayersState)
    }
}