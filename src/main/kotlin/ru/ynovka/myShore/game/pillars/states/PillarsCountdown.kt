package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.game.gameUtils.ActionbarTimer
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.utils.restrictToBlock
import ru.ynovka.myShore.game.GameState
import java.util.concurrent.CompletableFuture


class PillarsCountdown(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {
    var timer: ActionbarTimer.ActionbarTimerHandler? = null

    override fun onEnterState() {
        val state = this

        game.applyNextRoundGenerators()

        game.gameWorld.countdownPrepare { game.fsm.current === state }
            .thenCompose {
                if (game.fsm.current !== state) {
                    return@thenCompose CompletableFuture.completedFuture<Void>(null)
                }

                game.gameWorld.spawnPlayers(game)
            }
            .thenRun {
                scheduler.schedule {
                    if (game.fsm.current === state) {
                        startCountdown()
                    }
                }.global().once()
            }

        if (game.party != null) {
            // todo выдаём меню голосования только party.owner
        } else {
            // todo выдаём меню голосования всем игрокам
        }
    }

    private fun startCountdown() {
        timer = ActionbarTimer.startCountdownTimer(
            time = 10,
            game = game,
            state = this,
            onCompletion = { game, _ ->
                if (game.activePlayers.size >= 2) {
                    game.fsm.transitionTo(PillarsInProgress(game))
                } else {
                    game.fsm.transitionTo(PillarsWaitingForPlayers(game))
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
