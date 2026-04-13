package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.GameState


/**
 *
 * Этап знакомства игроков, длится ровно 1 минуту
 */
class WDIntroductionPlayers(game: WDGame) : GameState<WDPlayer, WDGame>(game) {
    /**
     * Отправляем сооющение в чат с членами страны
     */
    override fun onEnter() {
        game.gamePlayers.forEach { wdPlayer ->
            wdPlayer.country?.teleport(wdPlayer.player)
        }

        // Отсчёт 1 минута, до перехода к следующему этапу
        inst.server.scheduler.runTaskLater(inst, Runnable {
            game.fsm.transitionTo(WDNegotiations(game))
        }, 60 * 20L)
    }

    override fun onPlayerReconnect(gamePlayer: WDPlayer) {
        gamePlayer.country?.teleport(gamePlayer.player)
    }

    override fun canPlayerJoin(gamePlayer: WDPlayer): Boolean = false
}