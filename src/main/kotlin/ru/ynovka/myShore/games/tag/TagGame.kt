package ru.ynovka.myShore.games.tag

import ru.ynovka.myShore.games.GameId
import ru.ynovka.myShore.lobby.Lobby
import ru.ynovka.myShore.games.Game
import org.bukkit.entity.Player


class TagGame(val lobby: Lobby) : Game {
    override val id: GameId = GameId.TAG
    override val name: String = "Салочки"

    override fun join(player: Player) {

    }

    override fun leave(player: Player) {

    }
}