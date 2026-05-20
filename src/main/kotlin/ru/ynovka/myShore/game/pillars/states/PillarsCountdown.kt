package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.game.pillars.PillarsWorldManager
import ru.ynovka.myShore.game.gameUtils.ActionbarTimer
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsWorldOld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.utils.canMove
import org.bukkit.GameMode


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
class PillarsCountdown(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorldOld, PillarsGame>(game) {
    override fun onEnterState() {
        // Переводим спеков в игроков
        game.gamePlayers += game.spectatorPlayers
        game.spectatorPlayers.clear()

        // Очищаем мир
        game.gameWorld.countdownPrepare()
        // Спавн колб, столбов и площадки + телепорт игроков
        PillarsWorldManager.spawnPlayers(game)

        // Отключаем передвищение игрокам
        game.gamePlayers.asPlayers().forEach { player ->
            scheduler.schedule {
                player.canMove(false)
                player.gameMode = GameMode.ADVENTURE
            }.entity(player).once()
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
        // todo
        PillarsWorldManager.spawnPlayer(game, gamePlayer)
    }

    override fun onPlayerLeave(gamePlayer: PillarsPlayer) {
        // todo
    }
}