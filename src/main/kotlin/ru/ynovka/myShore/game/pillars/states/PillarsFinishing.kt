package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.game.GamePlayer.Companion.forEachOnlinePlayer
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.game.gameUtils.spawnFireworksAround
import ru.ynovka.myShore.game.gameUtils.ActionbarTimer
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsItems
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.event.EventManager
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.game.GameState
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player


class PillarsFinishing(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {
    private var automaticNextRoundStarted = false

    override fun onEnterState() {
        val winner = determineWinner()

        if (winner != null) {
            announceWinner(winner)
        }

        val event = game.party?.let { party ->
            EventManager.activeEvent?.takeIf { it.party === party }
        }

        game.moveSpectatorsToActive()

        if (event != null) {
            setupEventFinishing()

            return
        }

        ActionbarTimer.startCountdownTimer(
            time = 5,
            game = game,
            state = this,
            componentKey = "bar.myshore.new_round_in",
            onCompletion = { game, _ ->
                game.transitionToRoundStart()
            }
        )
    }

    override fun onPlayerJoin(gamePlayer: PillarsPlayer) {
        gamePlayer.withOnlinePlayer { player ->
            if (game.canOwnerControl(player)) {
                giveOwnerControls(player)
            }
        }
    }

    override fun onPlayerReconnect(gamePlayer: PillarsPlayer) = onPlayerJoin(gamePlayer)

    override fun onPlayerLeave(gamePlayer: PillarsPlayer) {
        val party = game.party ?: return
        val owner = Bukkit.getPlayer(party.owner)

        if (owner != null && game.hasPlayer(owner.uniqueId) && game.canOwnerControl(owner)) {
            game.getPlayer(owner.uniqueId)?.withOnlinePlayer(::giveOwnerControls)
            return
        }

        startAutomaticNextRound()
    }

    private fun setupEventFinishing() {
        val owner = game.party
            ?.owner
            ?.let(Bukkit::getPlayer)

        val ownerCanControl = owner != null &&
            game.canOwnerControl(owner) &&
            game.activePlayers.any { it.playerId == owner.uniqueId }

        game.activePlayers.forEach { pPlayer ->
            pPlayer.withOnlinePlayer { player ->
                if (ownerCanControl && player.uniqueId == owner.uniqueId) {
                    scheduler.schedule {
                        giveOwnerControls(player)
                    }.after(20L, Clock.TICKS).once()
                }
            }
        }

        if (!ownerCanControl) {
            startAutomaticNextRound()
        }
    }

    private fun giveOwnerControls(player: Player) {
        player.gameMode = GameMode.CREATIVE
        player.inventory.clear()
        player.inventory.setItem(0, PillarsItems.roundSettings.getStack(player))
        player.inventory.setItem(8, PillarsItems.nextRound.getStack(player))
    }

    private fun startAutomaticNextRound() {
        if (automaticNextRoundStarted) return
        automaticNextRoundStarted = true

        scheduler.schedule {
            if (game.isCurrentState(this)) {
                game.startNextRound()
            }
        }.global().after(1L, Clock.TICKS).once()
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

    override fun canPlayerJoin(gamePlayer: PillarsPlayer) = game.party != null

    override fun canPlayerReconnect(gamePlayer: PillarsPlayer) = true
}
