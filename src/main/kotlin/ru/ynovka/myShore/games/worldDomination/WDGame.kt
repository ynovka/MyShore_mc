package ru.ynovka.myShore.games.worldDomination

import ru.ynovka.myShore.games.GameId
import ru.ynovka.myShore.lobby.Lobby
import ru.ynovka.myShore.games.Game
import org.bukkit.entity.Player

class WDGame(val lobby: Lobby) : Game {
    override val gameId = GameId.WORLD_DOMINATION
    override val name = "Мировое Господство"
    /** Текущий раунд игры (не состояние) */
    var round = 0
    val ecology: Int
        get() = 100

    override fun join(player: Player) {

    }

    override fun leave(player: Player) {

    }

    override fun reconnect(player: Player) {

    }
}