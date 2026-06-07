package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.game.GamePlayer.Companion.forEachOnlinePlayer
import ru.ynovka.myShore.game.gameUtils.spawnFireworksAround
import ru.ynovka.myShore.game.gameUtils.ActionbarTimer
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.event.EventManager
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.game.GameManager
import ru.ynovka.myShore.game.GameState
import net.kyori.adventure.title.Title
import ru.ynovka.myShore.hub.Hub.toHub
import org.bukkit.Bukkit


class PillarsFinishing(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {

    override fun onEnterState() {
        val winner = determineWinner()

        if (winner != null) {
            announceWinner(winner)
        }

        val event = game.party?.let { party ->
            EventManager.activeEvent?.takeIf { it.party === party }
        }

        if (event != null) {
            game.activePlayers += game.spectatorPlayers
            game.spectatorPlayers.clear()

            ActionbarTimer.startCountdownTimer(
                time = 5,
                game = game,
                state = this,
                componentKey = "bar.myshore.new_round_in",
                onCompletion = { game, _ ->
                    val onlinePlayers = game.gamePlayers.mapNotNull { Bukkit.getPlayer(it.playerId) }
                    onlinePlayers.forEach { it.toHub() }

                    game.gamePlayers
                        .filter { Bukkit.getPlayer(it.playerId) == null }
                        .forEach { GameManager.leave(it.playerId) }

                    EventManager.onGameRoundFinished(game.party)
                }
            )

            return
        }

        game.activePlayers += game.spectatorPlayers
        game.spectatorPlayers.clear()

        ActionbarTimer.startCountdownTimer(
            time = 5,
            game = game,
            state = this,
            componentKey = "bar.myshore.new_round_in",
            onCompletion = { game, _ ->
                if (game.activePlayers.size >= 2) {
                    game.fsm.transitionTo(PillarsCountdown(game))
                } else {
                    game.fsm.transitionTo(PillarsWaitingForPlayers(game))
                }
            }
        )
    }

    private fun determineWinner(): PillarsPlayer? {
        return game.activePlayers
            .shuffled()
            .maxWithOrNull(
                compareBy<PillarsPlayer> { it.kills }
                    .thenBy { it.lastKnownY }
            )
    }

    private fun announceWinner(winner: PillarsPlayer) {
        winner.withOnlinePlayer { winnerPlayer ->
            val title = Title.title(
                Component.translatable(
                    "title.myshore.player.win",
                    Component.text(winnerPlayer.name)
                ),
                Component.empty()
            )

            val audience = game.activePlayers + game.spectatorPlayers

            audience.forEachOnlinePlayer { player ->
                scheduler.schedule {
                    player.showTitle(title)
                }.entity(player).once()
            }

            spawnFireworksAround(winnerPlayer)
        }
    }

    override fun canPlayerJoin(gamePlayer: PillarsPlayer) = false

    override fun canPlayerReconnect(gamePlayer: PillarsPlayer) = true
}
