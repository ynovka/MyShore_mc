package ru.ynovka.myShore.games.worldDomination.states

import com.github.darksoulq.abyssallib.extension.closeGui
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.text.actionBar.ActionBar
import ru.ynovka.myShore.utils.BossBarTimer
import ru.ynovka.myShore.games.GameState


// Ожидание игроков (нужно хотя бы 12)
class WDWaitingForPlayers(game: WDGame) : GameState<WDPlayer, WDGame>(game) {
    private val timer = BossBarTimer()

    private var started = false

    override fun onEnterState() {
        game.gamePlayers.forEach { gamePlayer ->
            val player = gamePlayer.player

            player.teleportAsync(WDGame.hubLoc)
            player.inventory.clear()
            timer.addPlayer(player)
        }

        tryStartTimer()
    }

    override fun onExitState() {
        timer.stop()
        started = false

        game.gamePlayers.forEach { wdPlayer ->
            ActionBar.clear(wdPlayer.player)
            wdPlayer.player.closeGui()
        }
    }

    override fun onPlayerJoin(gamePlayer: WDPlayer) {
        val player = gamePlayer.player

        player.teleportAsync(WDGame.hubLoc)
        player.inventory.clear()
        timer.addPlayer(player)

        tryStartTimer()
    }

    override fun onPlayerLeave(gamePlayer: WDPlayer) {
        val player = gamePlayer.player
        timer.removePlayer(player)
    }

    private fun tryStartTimer() {
        if (started) return
        if (game.gamePlayers.size < WDGame.MIN_PLAYERS) return

        started = true

        timer.start(
            totalSeconds = 10, // todo 60
            isActive = {
                game.gamePlayers.size >= WDGame.MIN_PLAYERS
            },
            onCancel = {
                started = false
            },
            onFinish = {
                started = false

                if (game.gamePlayers.size >= WDGame.MIN_PLAYERS) {
                    game.fsm.transitionTo(WDDistributionPlayers(game))
                }
            }
        )
    }
}