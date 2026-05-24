package ru.ynovka.myShore.game

import ru.ynovka.myShore.game.gameUtils.VisibilityGroup
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.party.PartyManager.Party
import org.bukkit.entity.Player
import org.bukkit.GameMode
import java.util.UUID


abstract class Game<P : GamePlayer, W : GameWorld>(
    val party: Party? = null  /** null  → публичная игра */
) {
    abstract val initialState: GameState<P, W, *>
    val fsm: GameFSM<P, W> by lazy { GameFSM(initialState).also { it.start() } }
    abstract val maxPlayers: Int
    abstract val gamePlayers: MutableSet<P>
    abstract val gameWorld: W
    val gameVisibilityGroup = VisibilityGroup()
    val exitedPlayers: MutableSet<P> = mutableSetOf()
    val spectatorPlayers: MutableSet<P> = mutableSetOf()

    val isPrivate: Boolean get() = party != null
    fun isEmpty(): Boolean = gamePlayers.isEmpty()
    fun isFull(): Boolean = gamePlayers.size >= maxPlayers

    fun hasActivePlayer(uuid: UUID): Boolean =
        gamePlayers.any { it.playerId == uuid }

    fun hasSpectator(uuid: UUID): Boolean =
        spectatorPlayers.any { it.playerId == uuid }

    fun hasExitedPlayer(uuid: UUID): Boolean =
        exitedPlayers.any { it.playerId == uuid }

    fun hasParticipant(uuid: UUID): Boolean =
        hasActivePlayer(uuid) || hasSpectator(uuid)

    fun onPlayerJoin(playerId: UUID) {
        gameVisibilityGroup.addViewer(playerId)

        val p = getOrCreatePlayer(playerId)

        val canJoin = !isFull() && fsm.canPlayerJoin(p)
        if (!canJoin) {
            spectatorPlayers.add(p)
            fsm.spectatorJoin(p)
            return
        }

        if (hasExitedPlayer(p.playerId)) {
            exitedPlayers.removeIf { it.playerId == p.playerId }
            gamePlayers.add(p)

            fsm.playerReconnect(p)
            handlePlayerReconnect(p)
            return
        }

        gamePlayers.add(p)

        fsm.playerJoin(p)
        handlePlayerJoin(p)
    }

    fun onPlayerLeave(playerId: UUID) {
        gameVisibilityGroup.removeViewer(playerId)

        val fromGame = gamePlayers.find { it.playerId == playerId }
        val fromSpec = spectatorPlayers.find { it.playerId == playerId }
        val p = fromGame ?: fromSpec ?: return

        gamePlayers.removeIf { it.playerId == playerId }
        spectatorPlayers.removeIf { it.playerId == playerId }

        if (fromGame != null) {
            exitedPlayers.add(p)
            fsm.playerLeave(p)
            handlePlayerLeave(p)
        }
    }

    fun movePlayerToSpectator(
        player: Player,
        reason: SpectatorReason = SpectatorReason.UNKNOWN
    ): Boolean {
        if (spectatorPlayers.any { it.playerId == player.uniqueId }) return false

        val p = gamePlayers.find { it.playerId == player.uniqueId } ?: return false

        if (!fsm.canPlayerBecomeSpectator(p, reason)) {
            return false
        }

        gamePlayers.removeIf { it.playerId == player.uniqueId }
        exitedPlayers.removeIf { it.playerId == player.uniqueId }
        spectatorPlayers.add(p)

        scheduler.schedule {
            player.gameMode = GameMode.SPECTATOR
        }.entity(player).once()
        fsm.playerBecomeSpectator(p, reason)
        handlePlayerBecomeSpectator(p, reason)

        return true
    }

    protected open fun handlePlayerJoin(gamePlayer: P)  {}
    protected open fun handlePlayerReconnect(gamePlayer: P)  {}
    protected open fun handlePlayerLeave(gamePlayer: P) {}
    protected open fun handlePlayerBecomeSpectator(gamePlayer: P, reason: SpectatorReason) {}

    abstract fun getOrCreatePlayer(playerId: UUID): P
}
