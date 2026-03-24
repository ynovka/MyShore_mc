package ru.ynovka.myShore.games

import org.bukkit.entity.Player
import ru.ynovka.myShore.party.PartyManager.Party
import java.util.UUID


abstract class Game<P : GamePlayer> {

    abstract val fsm: GameFSM<P>
    abstract val maxPlayers: Int
    abstract val players: MutableList<P>
    open val party: Party? = null  /** null  → публичная игра */

    fun start() = fsm.start(this)

    val isPrivate: Boolean get() = party != null
    fun isFull(): Boolean  = players.size >= maxPlayers
    fun isEmpty(): Boolean = players.isEmpty()
    fun hasPlayer(uuid: UUID): Boolean = players.any { it.player.uniqueId == uuid }

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