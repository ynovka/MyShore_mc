package ru.ynovka.myShore.game.worldDomination.states

import ru.ynovka.myShore.game.worldDomination.WDPlayer
import ru.ynovka.myShore.game.worldDomination.WDGame
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.game.GameWorld


/**
 * Этап распределния подведения итогов
 * Длится 1 минуту, по истечению которой игроков кикает в хаб
 */
class WDFinishingState(game: WDGame) : GameState<WDPlayer, GameWorld, WDGame>(game) {
    override fun onEnterState() { }

    override fun onExitState() { }

    override fun onPlayerJoin(gamePlayer: WDPlayer) { }

    override fun onPlayerReconnect(gamePlayer: WDPlayer) { }

    override fun onPlayerLeave(gamePlayer: WDPlayer) { }

    override fun canPlayerJoin(gamePlayer: WDPlayer): Boolean = false
}