package ru.ynovka.myShore.game

import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.gameUtils.VisibilityGroup
import ru.ynovka.myShore.party.PartyManager.Party
import org.bukkit.GameMode
import java.util.UUID


abstract class Game<P : GamePlayer, W : GameWorld>(
    val party: Party? = null
) {
    private var destroyed = false
    val isDestroyed: Boolean
        get() = destroyed

    abstract val initialState: GameState<P, W, *>
    val fsm: GameFSM<P, W> by lazy { GameFSM(initialState).also { it.start() } }

    abstract val gameWorld: W
    abstract val maxPlayers: Int

    val gameVisibilityGroup = VisibilityGroup()

    val gamePlayers: MutableSet<P> = mutableSetOf()
    val activePlayers: MutableSet<P> = mutableSetOf()
    val exitedPlayers: MutableSet<P> = mutableSetOf()
    val spectatorPlayers: MutableSet<P> = mutableSetOf()

    private val playersById: MutableMap<UUID, P> = mutableMapOf()

    val isPrivate: Boolean
        get() = party != null

    fun isEmpty(): Boolean =
        activePlayers.isEmpty() && spectatorPlayers.isEmpty()

    fun isFull(): Boolean =
        activePlayers.size >= maxPlayers

    fun hasPlayer(playerId: UUID): Boolean =
        playersById.containsKey(playerId)

    fun hasActivePlayer(playerId: UUID): Boolean {
        val player = playersById[playerId] ?: return false
        return player in activePlayers
    }

    fun hasSpectator(playerId: UUID): Boolean {
        val player = playersById[playerId] ?: return false
        return player in spectatorPlayers
    }

    fun hasExitedPlayer(playerId: UUID): Boolean {
        val player = playersById[playerId] ?: return false
        return player in exitedPlayers
    }

    fun getPlayer(playerId: UUID): P? =
        playersById[playerId]

    fun getOrCreatePlayer(playerId: UUID): P {
        check(!destroyed) { "Game is already destroyed" }

        playersById[playerId]?.let { return it }

        val player = createPlayer(playerId)

        playersById[playerId] = player
        gamePlayers.add(player)

        return player
    }

    abstract fun createPlayer(playerId: UUID): P

    fun onPlayerJoin(playerId: UUID) {
        println("onPlayerJoin 1")
        if (destroyed) return

        val gamePlayer = getOrCreatePlayer(playerId)

        if (gamePlayer in activePlayers || gamePlayer in spectatorPlayers) return
        println("onPlayerJoin 2")

        gameVisibilityGroup.addViewer(playerId)

        val canJoin = !isFull() && fsm.canPlayerJoin(gamePlayer)

        if (!canJoin) {
            movePlayerToSpectator(gamePlayer, SpectatorReason.GAME_FULL)
            fsm.spectatorJoin(gamePlayer)
            return
        }
        println("onPlayerJoin 3")

        if (exitedPlayers.remove(gamePlayer)) {
            activePlayers.add(gamePlayer)

            fsm.playerReconnect(gamePlayer)
            handlePlayerReconnect(gamePlayer)
            return
        }
        println("onPlayerJoin 4")

        activePlayers.add(gamePlayer)

        fsm.playerJoin(gamePlayer)
        handlePlayerJoin(gamePlayer)
        println("onPlayerJoin 5")
        println("activePlayers: $activePlayers")
        println("spectatorPlayers: $spectatorPlayers")
        println("onPlayerJoin 5")
    }

    fun onPlayerLeave(playerId: UUID) {
        println("onPlayerLeave 1")
        if (destroyed) return

        gameVisibilityGroup.removeViewer(playerId)

        val player = playersById[playerId] ?: return

        val wasActive = activePlayers.remove(player)
        val wasSpectator = spectatorPlayers.remove(player)

        if (!wasActive && !wasSpectator) return

        if (wasActive) {
            exitedPlayers.add(player)
            playersById.remove(playerId)

            fsm.playerLeave(player)
            handlePlayerLeave(player)
        }
        println("onPlayerLeave 2")
        println("activePlayers: $activePlayers")
        println("spectatorPlayers: $spectatorPlayers")
        println("onPlayerLeave 2")
    }

    fun movePlayerToSpectator(
        gamePlayer: P,
        reason: SpectatorReason = SpectatorReason.UNKNOWN
    ): Boolean {
        if (destroyed) return false

        val player = playersById[gamePlayer.playerId] ?: gamePlayer.also {
            playersById[it.playerId] = it
            gamePlayers.add(it)
        }

        if (player in spectatorPlayers) return false
        if (!fsm.canPlayerBecomeSpectator(player, reason)) return false

        activePlayers.remove(player)
        exitedPlayers.remove(player)
        spectatorPlayers.add(player)

        player.withOnlinePlayer { bukkitPlayer ->
            scheduler.schedule {
                bukkitPlayer.gameMode = GameMode.SPECTATOR
                bukkitPlayer.clearActivePotionEffects()
                bukkitPlayer.inventory.clear()
            }.entity(bukkitPlayer).once()
        }

        fsm.playerBecomeSpectator(player, reason)
        handlePlayerBecomeSpectator(player, reason)

        return true
    }

    fun destroy() {
        if (destroyed) return
        destroyed = true

        gameVisibilityGroup.clear()

        activePlayers.clear()
        exitedPlayers.clear()
        spectatorPlayers.clear()
        gamePlayers.clear()
        playersById.clear()
    }

    protected open fun handlePlayerJoin(gamePlayer: P) {}

    protected open fun handlePlayerReconnect(gamePlayer: P) {}

    protected open fun handlePlayerLeave(gamePlayer: P) {}

    protected open fun handlePlayerBecomeSpectator(
        gamePlayer: P,
        reason: SpectatorReason
    ) {}
}