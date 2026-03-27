package ru.ynovka.myShore.games

import org.bukkit.entity.Player
import ru.ynovka.myShore.party.PartyManager.Party
import java.util.UUID


abstract class Game<P : GamePlayer>(
    val party: Party? = null  /** null  → публичная игра */
) {

    abstract val initialState: GameState<P>
    val fsm: GameFSM<P> by lazy {
        GameFSM(initialState).also { it.start(this) }
    }
    abstract val maxPlayers: Int
    abstract val gamePlayers: MutableSet<P>
    val exitedPlayers: MutableSet<P> = mutableSetOf()
    val spectatorPlayers: MutableSet<P> = mutableSetOf()

    val isPrivate: Boolean get() = party != null
    fun isEmpty(): Boolean = gamePlayers.isEmpty()
    fun isFull(): Boolean = gamePlayers.size >= maxPlayers
    fun hasPlayer(uuid: UUID): Boolean = gamePlayers.any { it.playerId == uuid }
    private fun isExited(p: P) = exitedPlayers.any { it.playerId == p.playerId }

    fun onPlayerJoin(player: Player) {
        val p = getOrCreatePlayer(player)

        val canJoin = fsm.canPlayerJoin(p)
        if (!canJoin) {
            spectatorPlayers.add(p)
            fsm.spectatorJoin(p)
            return
        }

        if (isExited(p)) {
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

    protected open fun handlePlayerJoin(player: P)  {}
    protected open fun handlePlayerReconnect(player: P)  {}
    protected open fun handlePlayerLeave(player: P) {}

    abstract fun getOrCreatePlayer(player: Player): P
}
