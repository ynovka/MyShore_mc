package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.Game

/**
 * Этап знакомства игроков, длится ровно 1 минуту
 */
object WDIntroductionPlayers : GameState<WDPlayer> {
    /**
     * Отправляем сооющение в чат с членами страны
     */
    override fun onEnter(game: Game<WDPlayer>) {
        // Отсчёт 1 минута, до перехода к следующему этапу
        inst.server.scheduler.runTaskLater(inst, Runnable {
            game.fsm.transitionTo(WDNegotiations)
        }, 60 * 20L)
    }

    override fun onExit(game: Game<WDPlayer>) { }

    override fun onPlayerJoin(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerReconnect(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerLeave(game: Game<WDPlayer>, player: WDPlayer) { }
}