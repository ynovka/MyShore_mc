package ru.ynovka.myShore.games.worldDomination.states

import org.bukkit.entity.Player
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDGame

/**
 * Этап распределния игроков по странам
 * Снача определяется кол-во стран (кол-во игроков / 2, max 10)
 * Для тестов - минимум 2 игрока (2с по 1и)
 * Минимум 12 игроков (6с по 2и)
 * Максимум 50 игроков (10с по 5и)
 */
object WDDistributionPlayersState : GameState<WDGame> {
    override fun onStateStart(game: WDGame) {}

    override fun onPlayerJoin(game: WDGame, player: Player) {}
}