package ru.ynovka.myShore.game

import org.bukkit.entity.Player
import ru.ynovka.myShore.visibilityGroup.VisibilityGroup
import ru.ynovka.myShore.party.PartyManager.Party
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

    fun onPlayerJoin(player: Player) {
        player.inventory.close()
        gameVisibilityGroup.addViewer(player.uniqueId)

        val p = getOrCreatePlayer(player)

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

    fun onPlayerLeave(player: Player) {
        val uuid = player.uniqueId
        gameVisibilityGroup.removeViewer(uuid)

        val fromGame = gamePlayers.find { it.playerId == uuid }
        val fromSpec = spectatorPlayers.find { it.playerId == uuid }
        val p = fromGame ?: fromSpec ?: return

        gamePlayers.removeIf { it.playerId == uuid }
        spectatorPlayers.removeIf { it.playerId == uuid }

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

        fsm.playerBecomeSpectator(p, reason)
        handlePlayerBecomeSpectator(p, reason)

        return true
    }

    protected open fun handlePlayerJoin(player: P)  {}
    protected open fun handlePlayerReconnect(player: P)  {}
    protected open fun handlePlayerLeave(player: P) {}
    protected open fun handlePlayerBecomeSpectator(player: P, reason: SpectatorReason) {}

    abstract fun getOrCreatePlayer(player: Player): P
}
