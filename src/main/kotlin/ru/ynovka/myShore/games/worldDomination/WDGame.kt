package ru.ynovka.myShore.games.worldDomination

import org.bukkit.entity.Player
import ru.ynovka.myShore.games.worldDomination.states.WDWaitingForPlayersState
import ru.ynovka.myShore.games.GameFSM
import ru.ynovka.myShore.games.Game


class WDGame : Game<WDPlayer>() {
    override val maxPlayers = 50
    override val players: MutableList<WDPlayer> = mutableListOf()
    override val fsm = GameFSM(WDWaitingForPlayersState)

    /** Текущий раунд игры */
    var round = 0
    val ecology: Int
        get() = 100


    override fun getOrCreatePlayer(player: Player): WDPlayer =WDPlayer(player)
}