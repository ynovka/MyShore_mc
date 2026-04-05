package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.Game

/**
 * Этап знакомства игроков, длится ровно 1 минуту
 */
class WDIntroductionPlayers(game: Game<WDPlayer>) : GameState<WDPlayer>(game) {
    /**
     * Отправляем сооющение в чат с членами страны
     */
    override fun onEnter() {
        // Отсчёт 1 минута, до перехода к следующему этапу
        inst.server.scheduler.runTaskLater(inst, Runnable {
            game.fsm.transitionTo(WDNegotiations(game))
        }, 60 * 20L)
    }

    override fun onExit() { }

    override fun onPlayerJoin(player: WDPlayer) { }

    override fun onPlayerReconnect(player: WDPlayer) { }

    override fun onPlayerLeave(player: WDPlayer) { }
}