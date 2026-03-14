package ru.ynovka.myShore.games

import ru.ynovka.myShore.games.tag.TagGame
import org.bukkit.entity.Player
import java.util.concurrent.atomic.AtomicLong


interface Game {
    val gameId: GameId
    val name: String
    fun join(player: Player)
    fun leave(player: Player)
}

object GameIdGenerator {
    private val counter = AtomicLong(System.currentTimeMillis())

    fun next(): Long = counter.incrementAndGet()
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