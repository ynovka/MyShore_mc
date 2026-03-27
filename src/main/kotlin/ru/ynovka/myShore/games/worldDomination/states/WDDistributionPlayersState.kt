package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.worldDomination.entity.CountryType
import ru.ynovka.myShore.games.worldDomination.entity.Country
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.Game

/**
 * Этап распределния игроков по странам
 * Снача определяется кол-во стран (кол-во игроков / 2, max 10)
 * Для тестов - минимум 2 игрока (2с по 1и)
 * Минимум 12 игроков (6с по 2и)
 * Максимум 50 игроков (10с по 5и)
 */
object WDDistributionPlayersState : GameState<WDPlayer> {
    override fun onEnter(game: Game<WDPlayer>) {
        val countriesCount = (game.gamePlayers.size / 2).coerceIn(2..10)
        val presidents = game.gamePlayers.shuffled().take(countriesCount)
        val countries = CountryType.entries.shuffled().take(countriesCount)
        presidents.forEachIndexed { i, president ->
            Country(
                president,
                countries[i]
            )
        }
    }

    override fun onExit(game: Game<WDPlayer>) { }

    override fun onPlayerJoin(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerReconnect(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerLeave(game: Game<WDPlayer>, player: WDPlayer) { }
}