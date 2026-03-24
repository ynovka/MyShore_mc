package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDPlayer

/**
 * Этап распределния игроков по странам
 * Снача определяется кол-во стран (кол-во игроков / 2, max 10)
 * Для тестов - минимум 2 игрока (2с по 1и)
 * Минимум 12 игроков (6с по 2и)
 * Максимум 50 игроков (10с по 5и)
 */
object WDDistributionPlayersState : GameState<WDPlayer> {
    override fun onEnter(game: Game<WDPlayer>) { }

    override fun onExit(game: Game<WDPlayer>) { }

    override fun onPlayerJoin(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerReconnect(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerLeave(game: Game<WDPlayer>, player: WDPlayer) { }
}