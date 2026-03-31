package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDPlayer

/**
 * Этап распределния подведения итогов
 * Длится 1 минуту, по истечению которой игроков кикает в хаб
 */
object WDFinishingState : GameState<WDPlayer> {
    override fun onEnter(game: Game<WDPlayer>) { }

    override fun onExit(game: Game<WDPlayer>) { }

    override fun onPlayerJoin(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerReconnect(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerLeave(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun canPlayerJoin(game: Game<WDPlayer>, player: WDPlayer) = false
}