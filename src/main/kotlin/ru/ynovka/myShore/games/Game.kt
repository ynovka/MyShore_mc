package ru.ynovka.myShore.games

import org.bukkit.entity.Player


interface Game {
    val id: GameId
    val name: String
    fun join(player: Player)
    fun leave(player: Player)
}


enum class GameId(val maxPlayers: Int) {
    TAG(5)
}