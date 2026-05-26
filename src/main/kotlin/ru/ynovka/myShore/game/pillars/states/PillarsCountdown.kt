package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.game.gameUtils.ActionbarTimer
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.utils.restrictToBlock
import ru.ynovka.myShore.game.GameState


class PillarsCountdown(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {
    var timer: ActionbarTimer.ActionbarTimerHandler? = null

    override fun onEnterState() {
        // Очищаем мир
        game.gameWorld.countdownPrepare().thenRun {
            // Спавн колб, столбов и площадки + телепорт игроков + Отключаем передвищение игрокам + режим игры adv
            game.gameWorld.spawnPlayers(game)
        }

        // Начинаем игру
        timer = ActionbarTimer.startCountdownTimer(
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
            gamePlayer.withOnlinePlayer { player ->
                scheduler.schedule {
                    player.restrictToBlock(true)
                }.entity(player).once()
            }
        }
    }

    override fun onPlayerLeave(gamePlayer: PillarsPlayer) {
        if (game.activePlayers.size <= 1) {
            timer?.cancel()
            timer = null
            game.fsm.transitionTo(PillarsWaitingForPlayers(game))
        }
        val world = game.gameWorld.get() ?: return
        game.gameWorld.removePlayerPillar(gamePlayer.playerId, world)
    }
}