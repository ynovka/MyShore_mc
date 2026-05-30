package ru.ynovka.myShore.game

import ru.ynovka.myShore.MyShore.Companion.scheduler
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import java.util.UUID


abstract class GamePlayer(
    val playerId: UUID
) {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GamePlayer) return false

        return playerId == other.playerId
    }

    final override fun hashCode(): Int {
        return playerId.hashCode()
    }

    fun withOnlinePlayer(action: (Player) -> Unit) {
        scheduler.schedule {
            val player = Bukkit.getPlayer(playerId) ?: return@schedule
            action(player)
        }.global().once()
    }

    fun asPlayer() = Bukkit.getPlayer(playerId)

    companion object {
        fun Iterable<GamePlayer>.forEachOnlinePlayer(action: (Player) -> Unit) {
            scheduler.schedule {
                mapNotNull { Bukkit.getPlayer(it.playerId) }
                    .forEach(action)
            }.global().once()
        }

        fun Iterable<GamePlayer>.withOnlinePlayers(action: (List<Player>) -> Unit) {
            scheduler.schedule {
                val players = mapNotNull { Bukkit.getPlayer(it.playerId) }
                action(players)
            }.global().once()
        }
    }
}