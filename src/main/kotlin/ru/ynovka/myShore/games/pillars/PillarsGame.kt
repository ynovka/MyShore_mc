package ru.ynovka.myShore.games.pillars

import org.bukkit.entity.Player
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameId
import ru.ynovka.myShore.lobby.Lobby

class PillarsGame(val lobby: Lobby) : Game {
    override val id = GameId.PILLARS
    override val name = "Столбы"

    override fun join(player: Player) {

    }

    override fun leave(player: Player) {

    }
}