package ru.ynovka.myShore.game

import java.util.UUID


class GameRoster<P : GamePlayer>(
    private val createPlayer: (UUID) -> P
) {
    val gamePlayers: MutableSet<P> = mutableSetOf()
    val activePlayers: MutableSet<P> = mutableSetOf()
    val exitedPlayers: MutableSet<P> = mutableSetOf()
    val exitedSpectatorPlayers: MutableSet<P> = mutableSetOf()
    val spectatorPlayers: MutableSet<P> = mutableSetOf()

    private val playersById: MutableMap<UUID, P> = mutableMapOf()

    fun isEmpty(): Boolean =
        activePlayers.isEmpty() && spectatorPlayers.isEmpty()

    fun isFull(maxPlayers: Int): Boolean =
        activePlayers.size >= maxPlayers

    fun hasPlayer(playerId: UUID): Boolean =
        playersById.containsKey(playerId)

    fun hasActivePlayer(playerId: UUID): Boolean =
        playersById[playerId] in activePlayers

    fun hasSpectator(playerId: UUID): Boolean =
        playersById[playerId] in spectatorPlayers

    fun hasExitedPlayer(playerId: UUID): Boolean =
        exitedPlayers.any { it.playerId == playerId } ||
            exitedSpectatorPlayers.any { it.playerId == playerId }

    fun getPlayer(playerId: UUID): P? =
        playersById[playerId]

    fun getPlayers(): Set<UUID> =
        playersById.keys

    fun getOrCreatePlayer(playerId: UUID): P {
        playersById[playerId]?.let { return it }

        val player = createPlayer(playerId)
        rememberPlayer(player)
        return player
    }

    fun createDetachedPlayer(playerId: UUID): P =
        playersById[playerId] ?: createPlayer(playerId)

    fun rememberPlayer(gamePlayer: P): P {
        playersById[gamePlayer.playerId]?.let { return it }

        playersById[gamePlayer.playerId] = gamePlayer
        gamePlayers.add(gamePlayer)
        return gamePlayer
    }

    fun forgetOnlinePlayer(playerId: UUID) {
        playersById.remove(playerId)
    }

    fun clear() {
        activePlayers.clear()
        exitedPlayers.clear()
        exitedSpectatorPlayers.clear()
        spectatorPlayers.clear()
        gamePlayers.clear()
        playersById.clear()
    }
}
