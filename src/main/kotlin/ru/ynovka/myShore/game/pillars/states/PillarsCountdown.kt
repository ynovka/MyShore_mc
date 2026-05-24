package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.game.gameUtils.ActionbarTimer
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.utils.restrictToBlock
import ru.ynovka.myShore.game.GameState


/**
 * Стадия обратного отсчёта 10 секунд, парралельно голосование за режим игры
 * Голосование - меню с 3 разделами:
 *  - точки спавна (кольцо, медовые соты)
 *  - игровая карта (определяет пол, и декор столбов):
 *   - стандартная (столбы из бедрока, 64 блока высотой, без платформы)
 *   - бездна (стобы из бедрока, 4 блока высотой, без платформы)
 *   - паутина (сандартная + основание из булыжника с паутиной + пауки)
 *     // Примечание, что мобы спавнятся разово в начале раунда, без спавнеров и в дневное время суток
 *   - батут (сандартная + основание из шума блоков слизи и изумрудов + слизни)
 */
class PillarsCountdown(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {
    override fun onEnterState() {
        println("PillarsCountdown 1")
        // Очищаем мир
        game.gameWorld.countdownPrepare().thenRun {
            // Спавн колб, столбов и площадки + телепорт игроков + Отключаем передвищение игрокам + режим игры adv
            game.gameWorld.spawnPlayers(game)
        }

        // Начинаем игру
        ActionbarTimer.startCountdownTimer(
            time = 10,
            game = game,
            state = this,
            onCompletion = { game, _ ->
                if (game.gamePlayers.size >= 2) {
                    game.fsm.transitionTo(PillarsInProgress(game))
                }
            }
        )
    }

    override fun onPlayerJoin(gamePlayer: PillarsPlayer) {
        game.gameWorld.spawnPlayer(game, gamePlayer).thenRun {
            val player = gamePlayer.playerOrNull
            player?.let {
                scheduler.schedule {
                    player.restrictToBlock(true)
                }.entity(player).once()
            }
        }
    }

    override fun onPlayerLeave(gamePlayer: PillarsPlayer) {
        val world = game.gameWorld.get() ?: return
        game.gameWorld.removePlayerPillar(gamePlayer.playerId, world)
    }
}