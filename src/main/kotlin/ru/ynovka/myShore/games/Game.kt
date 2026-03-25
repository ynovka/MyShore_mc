package ru.ynovka.myShore.games

import org.bukkit.entity.Player
import ru.ynovka.myShore.party.PartyManager.Party
import java.util.UUID


abstract class Game<P : GamePlayer>(
    val party: Party? = null  /** null  → публичная игра */
) {

    abstract val fsm: GameFSM<P>
    abstract val maxPlayers: Int
    abstract val gamePlayers: MutableList<P>

    fun start() = fsm.start(this)

    val isPrivate: Boolean get() = party != null
    fun isFull(): Boolean  = gamePlayers.size >= maxPlayers
    fun isEmpty(): Boolean = gamePlayers.isEmpty()
    fun hasPlayer(uuid: UUID): Boolean = gamePlayers.any { it.playerId == uuid }

    fun onPlayerJoin(player: Player) {
        val p = getOrCreatePlayer(player)
        fsm.playerJoin(p)
        handlePlayerJoin(p)
    }

    fun onPlayerReconnect(player: Player) {
        val p = getOrCreatePlayer(player)
        fsm.playerJoin(p)
        handlePlayerReconnect(p)
    }

    fun onPlayerLeave(player: Player) {
        val p = getOrCreatePlayer(player)
        fsm.playerLeave(p)
        handlePlayerLeave(p)
    }

    protected open fun handlePlayerJoin(player: P)  {}
    protected open fun handlePlayerReconnect(player: P)  {}
    protected open fun handlePlayerLeave(player: P) {}

    abstract fun getOrCreatePlayer(player: Player): P
}
