package ru.ynovka.myShore.games.worldDomination.states

import org.bukkit.entity.Player
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDGame

/**
 * Этап распределния подведения итогов
 * Длится 1 минуту, по истечению которой игроков кикает в хаб
 */
object WDFinishingState : GameState<WDGame> {
    override fun onStateStart(game: WDGame) {}

    override fun onPlayerJoin(game: WDGame, player: Player) {}
}