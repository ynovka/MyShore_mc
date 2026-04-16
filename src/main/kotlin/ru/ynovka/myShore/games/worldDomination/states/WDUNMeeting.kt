package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.games.worldDomination.WDPlayer


/**
 * Этап совещания ООН
 * Длится от 6 до 10 минут (кол-во стран * 1 минута)
 * (возможно нужно дать 10 секунд на подтверждения выступления, если не подтвердить выступление -
 * страну переместит в конец выступления, работает 1 раз за совещание, иначе речь пропускается)
 * В этот период каждой стране даётся 1 минута на любую речь
 * Порядок стран для выступления:
 * - прилетело больше бомб
 * - уровень развития
 * - название по алфавиту
 */
class WDUNMeeting(game: WDGame) : GameState<WDPlayer, WDGame>(game) {
    override fun onEnterState() {
        game.gamePlayers.forEach { wdPlayer ->
            game.gameVisibilityGroup.addViewer(wdPlayer.playerId)
        }

        game.countries.forEachIndexed { idx, country ->
            inst.server.scheduler.runTaskLater(inst, Runnable {
                // телепортируем страну на сцену
                // Даём ей право говорить в plasmo
                inst.server.scheduler.runTaskLater(inst, Runnable {

                }, 60 * 20L)
            }, idx * 60 * 20L + 1)
        }

        inst.server.scheduler.runTaskLater(inst, Runnable {
            // Проверяем текущий раунд,
            // если это был заключительный 5-ый раунд
            // или экология упала до 0
            // или осталась одна единственная трана - завершаем игру
            if (game.round == 5
                || game.ecology <= 0
                || game.countries.count { it.isAlive } == 1)
            {
                game.fsm.transitionTo(WDFinishingState(game))
            } else {
                game.fsm.transitionTo(WDUNMeeting(game))
            }
        }, game.countries.size * 60 * 20L)
    }

    override fun onExitState() { }

    override fun onPlayerReconnect(gamePlayer: WDPlayer) {
        game.gameVisibilityGroup.addViewer(gamePlayer.playerId)
        // Добавляем его в plasmo
    }

    override fun onPlayerLeave(gamePlayer: WDPlayer) { }

    override fun canPlayerJoin(gamePlayer: WDPlayer): Boolean = false
}