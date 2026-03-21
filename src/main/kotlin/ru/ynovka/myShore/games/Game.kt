package ru.ynovka.myShore.games

import org.bukkit.entity.Player
import java.util.concurrent.atomic.AtomicLong


interface Game {
    val gameId: GameId
    val name: String
    fun join(player: Player)
    fun leave(player: Player)
    fun reconnect(player: Player) {} // todo сделать команду reconnect и подсказку игроку если есть реализация функции
}

object GameIdGenerator {
    private val counter = AtomicLong(System.currentTimeMillis())

    fun next(): Long = counter.incrementAndGet()
}

enum class GameId(val maxPlayers: Int) {
    TAG(5),
    PILLARS(8),
    WORLD_DOMINATION(50) // 10 стран по 5 чел
}

interface GameState<G : Game> {
    fun onStateStart(game: G) {}
    fun onStateEnd(game: G) {}
    fun onPlayerJoin(game: G, player: Player) {}
    fun onPlayerLeave(game: G, player: Player) {}
}