package ru.ynovka.myShore.game

import ru.ynovka.myShore.game.gameUtils.VisibilityGroup
import ru.ynovka.myShore.party.PartyManager.Party
import java.util.UUID


abstract class Game<P : GamePlayer, W : GameWorld>(
    val party: Party? = null
) {
    private var destroyed = false
    val isDestroyed: Boolean
        get() = destroyed

    abstract val initialState: GameState<P, W, *>
    val fsm: GameFSM<P, W> by lazy { GameFSM(initialState).also { it.start() } }
    val currentState: GameState<P, W, *>
        get() = fsm.current

    abstract val gameWorld: W
    abstract val maxPlayers: Int

    val visibilityGroup = VisibilityGroup()
    val gameVisibilityGroup: VisibilityGroup
        get() = visibilityGroup

    private val roster = GameRoster { playerId -> createPlayer(playerId) }
    private val playerLifecycle = GamePlayerLifecycle(
        roster = roster,
        visibilityGroup = visibilityGroup,
        fsm = { fsm },
        isDestroyed = { destroyed },
        maxPlayers = { maxPlayers },
        onPlayerJoined = { handlePlayerJoin(it) },
        onPlayerReconnected = { handlePlayerReconnect(it) },
        onPlayerLeft = { handlePlayerLeave(it) },
        onPlayerBecameSpectator = { player, reason ->
            handlePlayerBecomeSpectator(player, reason)
        }
    )

    val gamePlayers: MutableSet<P>
        get() = roster.gamePlayers
    val activePlayers: MutableSet<P>
        get() = roster.activePlayers
    val exitedPlayers: MutableSet<P>
        get() = roster.exitedPlayers
    val exitedSpectatorPlayers: MutableSet<P>
        get() = roster.exitedSpectatorPlayers
    val spectatorPlayers: MutableSet<P>
        get() = roster.spectatorPlayers

    val isPrivate: Boolean
        get() = party != null

    fun isEmpty(): Boolean =
        roster.isEmpty()

    fun isFull(): Boolean =
        roster.isFull(maxPlayers)

    fun hasPlayer(playerId: UUID): Boolean =
        roster.hasPlayer(playerId)

    fun hasActivePlayer(playerId: UUID): Boolean =
        roster.hasActivePlayer(playerId)

    fun hasSpectator(playerId: UUID): Boolean =
        roster.hasSpectator(playerId)

    fun hasExitedPlayer(playerId: UUID): Boolean =
        roster.hasExitedPlayer(playerId)

    fun getPlayer(playerId: UUID): P? =
        roster.getPlayer(playerId)

    fun getPlayers(): Set<UUID> =
        roster.getPlayers()

    fun getOrCreatePlayer(playerId: UUID): P {
        check(!destroyed) { "Game is already destroyed" }
        return roster.getOrCreatePlayer(playerId)
    }

    abstract fun createPlayer(playerId: UUID): P

    fun canAcceptNewPlayer(playerId: UUID): Boolean =
        playerLifecycle.canAcceptNewPlayer(playerId)

    fun onPlayerJoin(playerId: UUID) =
        playerLifecycle.onPlayerJoin(playerId)

    fun onPlayerLeave(playerId: UUID) =
        playerLifecycle.onPlayerLeave(playerId)

    fun movePlayerToSpectator(
        gamePlayer: P,
        reason: SpectatorReason = SpectatorReason.UNKNOWN
    ): Boolean =
        playerLifecycle.movePlayerToSpectator(gamePlayer, reason)

    fun moveSpectatorsToActive() =
        playerLifecycle.moveSpectatorsToActive()

    fun refreshSpectatorVisibility() =
        playerLifecycle.refreshSpectatorVisibility()

    fun transitionTo(next: GameState<P, W, *>) =
        fsm.transitionTo(next)

    fun isCurrentState(state: GameState<P, W, *>): Boolean =
        currentState === state

    inline fun <reified S : GameState<*, *, *>> isInState(): Boolean =
        currentState is S

    fun destroy() {
        if (destroyed) return
        destroyed = true

        visibilityGroup.clear()
        roster.clear()
    }

    protected open fun handlePlayerJoin(gamePlayer: P) {}

    protected open fun handlePlayerReconnect(gamePlayer: P) {}

    protected open fun handlePlayerLeave(gamePlayer: P) {}

    protected open fun handlePlayerBecomeSpectator(
        gamePlayer: P,
        reason: SpectatorReason
    ) {}
}
