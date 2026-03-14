package ru.ynovka.myShore.games.pillars

import ru.ynovka.myShore.games.GameId
import ru.ynovka.myShore.lobby.Lobby
import ru.ynovka.myShore.games.Game
import org.bukkit.entity.Player


class PillarsGame(val lobby: Lobby) : Game {
    override val gameId = GameId.PILLARS
    override val name = "Столбы"

    override fun join(player: Player) {

    }

    override fun leave(player: Player) {

    }
}