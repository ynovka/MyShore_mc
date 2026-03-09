package ru.ynovka.myShore.games

import org.bukkit.entity.Player
import ru.ynovka.myShore.games.tag.TagGame


interface Game {
    val id: GameId
    val name: String
    fun join(player: Player)
    fun leave(player: Player)
}


enum class GameId(val maxPlayers: Int) {
    TAG(5),
    PILLARS(8)
}

interface GameState {
    fun onStateStart(game: TagGame) {}
    fun onStateEnd(game: TagGame) {}
    fun onPlayerJoin(game: TagGame, player: Player) {}
    fun onPlayerLeave(game: TagGame, player: Player) {}
}