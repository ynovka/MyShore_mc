package ru.ynovka.myShore.games.worldDomination

import org.bukkit.entity.Player
import ru.ynovka.myShore.games.worldDomination.states.WDWaitingForPlayersState
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.worldDomination.entity.Country


class WDGame : Game<WDPlayer>() {
    override val initialState = WDWaitingForPlayersState
    override val maxPlayers = 50
    override val gamePlayers: MutableSet<WDPlayer> = mutableSetOf()

    /** Список стран */
    val countries: MutableSet<Country> = mutableSetOf()
    /** Текущий раунд игры */
    var round = 0
    val ecology: Int
        get() = 100


    override fun getOrCreatePlayer(player: Player): WDPlayer =
        gamePlayers.firstOrNull { it.playerId == player.uniqueId} ?: WDPlayer(player.uniqueId)

    companion object {
        const val MIN_PLAYERS = 2 // todo заменить на 12
    }
}