package ru.ynovka.myShore.game

import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.gameUtils.VisibilityGroup
import ru.ynovka.myShore.party.PartyManager.Party
import org.bukkit.GameMode
import java.util.UUID


abstract class Game<P : GamePlayer, W : GameWorld>(
    val party: Party? = null // null -> публичная игра
) {
    abstract val initialState: GameState<P, W, *>
    val fsm: GameFSM<P, W> by lazy { GameFSM(initialState).also { it.start() } }

    abstract val gameWorld: W
    abstract val maxPlayers: Int

    val gameVisibilityGroup = VisibilityGroup()

    /**
     * Все игроки, которые когда-либо были созданы для этой игры.
     */
    val gamePlayers: MutableSet<P> = mutableSetOf()

    /**
     * Игроки, которые сейчас играют.
     */
    val activePlayers: MutableSet<P> = mutableSetOf()

    /**
     * Игроки, которые вышли из игры, но могут вернуться.
     */
    val exitedPlayers: MutableSet<P> = mutableSetOf()

    /**
     * Игроки, которые сейчас являются зрителями.
     */
    val spectatorPlayers: MutableSet<P> = mutableSetOf()

    /**
     * Основной индекс игроков.
     *
     * Нужен, чтобы не делать activePlayers.find { it.playerId == uuid }
     * и не создавать дубликаты GamePlayer с одним UUID.
     */
    private val playersById: MutableMap<UUID, P> = mutableMapOf()

    val isPrivate: Boolean
        get() = party != null

    fun isEmpty(): Boolean =
        activePlayers.isEmpty()

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
        playersById[playerId]?.let { return it }

        val player = createPlayer(playerId)

        playersById[playerId] = player
        gamePlayers.add(player)

        return player
    }

    abstract fun createPlayer(playerId: UUID): P

    fun onPlayerJoin(playerId: UUID) {
        val gamePlayer = getOrCreatePlayer(playerId)

        if (gamePlayer in activePlayers || gamePlayer in spectatorPlayers) return

        gameVisibilityGroup.addViewer(playerId)
        val canJoin = !isFull() && fsm.canPlayerJoin(gamePlayer)

        if (!canJoin) {
            movePlayerToSpectator(gamePlayer, SpectatorReason.GAME_FULL)
            fsm.spectatorJoin(gamePlayer)
            return
        }

        if (exitedPlayers.remove(gamePlayer)) {
            activePlayers.add(gamePlayer)

            fsm.playerReconnect(gamePlayer)
            handlePlayerReconnect(gamePlayer)
            return
        }

        activePlayers.add(gamePlayer)

        fsm.playerJoin(gamePlayer)
    }

    fun onPlayerLeave(playerId: UUID) {
        gameVisibilityGroup.removeViewer(playerId)

        val player = playersById[playerId] ?: return

        playersById.remove(playerId)
        val wasActive = activePlayers.remove(player)
        val wasSpectator = spectatorPlayers.remove(player)

        if (!wasActive && !wasSpectator) return

        if (wasActive) {
            exitedPlayers.add(player)

            fsm.playerLeave(player)
            handlePlayerLeave(player)
        }
    }

    fun movePlayerToSpectator(
        gamePlayer: P,
        reason: SpectatorReason = SpectatorReason.UNKNOWN
    ): Boolean {
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
                bukkitPlayer.activePotionEffects.clear()
                bukkitPlayer.inventory.clear()
            }.entity(bukkitPlayer).once()
        }

        fsm.playerBecomeSpectator(player, reason)
        handlePlayerBecomeSpectator(player, reason)

        return true
    }

    protected open fun handlePlayerJoin(gamePlayer: P) {}

    protected open fun handlePlayerReconnect(gamePlayer: P) {}

    protected open fun handlePlayerLeave(gamePlayer: P) {}

    protected open fun handlePlayerBecomeSpectator(
        gamePlayer: P,
        reason: SpectatorReason
    ) {}
}