package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.game.gameUtils.ActionbarTimer
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.game.GameState


class PillarsFinishing(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {

    override fun onEnterState() {
        // Переводим спеков в игроков
        game.gamePlayers += game.spectatorPlayers
        game.spectatorPlayers.clear()

        ActionbarTimer.startCountdownTimer(
            time = 5,
            game = game,
            state = this,
            componentKey = "bar.myshore.new_round_in",
            onCompletion = { game, _ ->
                println("PillarsFinishing 1")
                if (game.gamePlayers.size >= 2) {
                    println("PillarsFinishing 2")
                    game.fsm.transitionTo(PillarsCountdown(game))
                }
            }
        )
    }

    override fun canPlayerJoin(gamePlayer: PillarsPlayer) = false
}