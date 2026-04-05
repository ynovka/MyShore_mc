package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDPlayer

/**
 * Этап распределния подведения итогов
 * Длится 1 минуту, по истечению которой игроков кикает в хаб
 */
class WDFinishingState(game: Game<WDPlayer>) : GameState<WDPlayer>(game) {
    override fun onEnter() { }

    override fun onExit() { }

    override fun onPlayerJoin(player: WDPlayer) { }

    override fun onPlayerReconnect(player: WDPlayer) { }

    override fun onPlayerLeave(player: WDPlayer) { }

    override fun canPlayerJoin(player: WDPlayer): Boolean = false
}