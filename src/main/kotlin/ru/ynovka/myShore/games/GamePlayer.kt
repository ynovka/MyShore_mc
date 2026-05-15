package ru.ynovka.myShore.games

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

abstract class GamePlayer(
    val playerId: UUID
) {
    val playerOrNull: Player?
        get() = Bukkit.getPlayer(playerId)

    val player: Player
        get() = Bukkit.getPlayer(playerId) ?: throw IllegalStateException("Player not found")

    companion object {
        fun Iterable<GamePlayer>.asPlayers(): List<Player> = mapNotNull { it.playerOrNull }
    }
}