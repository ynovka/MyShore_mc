package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.games.GameState


/**
 * Этап распределния подведения итогов
 * Длится 1 минуту, по истечению которой игроков кикает в хаб
 */
class WDFinishingState(game: WDGame) : GameState<WDPlayer, WDGame>(game) {
    override fun onEnter() { }

    override fun onExit() { }

    override fun onPlayerJoin(gamePlayer: WDPlayer) { }

    override fun onPlayerReconnect(gamePlayer: WDPlayer) { }

    override fun onPlayerLeave(gamePlayer: WDPlayer) { }

    override fun canPlayerJoin(gamePlayer: WDPlayer): Boolean = false
}