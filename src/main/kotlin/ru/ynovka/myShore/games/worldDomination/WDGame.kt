package ru.ynovka.myShore.games.worldDomination

import ru.ynovka.myShore.games.worldDomination.states.WDWaitingForPlayers
import ru.ynovka.myShore.games.worldDomination.entity.WDGameHistory
import ru.ynovka.myShore.games.worldDomination.entity.Country
import ru.ynovka.myShore.games.GameManager
import ru.ynovka.myShore.games.GameWorld
import ru.ynovka.myShore.games.Game
import org.bukkit.entity.Player
import org.bukkit.Location


class WDGame : Game<WDPlayer, GameWorld>() {
    override val initialState = WDWaitingForPlayers(this)
    override val maxPlayers = 50
    override val gamePlayers: MutableSet<WDPlayer> = mutableSetOf()
    override val gameWorld: WDWorld = WDWorld

    /** Текущий раунд игры */
    var round = 0
    /** Список стран */
    val countries: MutableSet<Country> = mutableSetOf()
    /** Мировой уровень экологии */
    var ecology: Double = ECOLOGY_START
        set(value) { field = value.coerceIn(0.0, 1.0) }
    /** История действий всех стран за игру */
    val history = WDGameHistory()


    override fun getOrCreatePlayer(player: Player): WDPlayer =
        gamePlayers.firstOrNull { it.playerId == player.uniqueId} ?: WDPlayer(player.uniqueId)

    companion object {
        const val MIN_PLAYERS = 1 // todo заменить на 12
        const val ECOLOGY_START = 0.80

        val hubLoc: Location
            get() = WDWorld.hubLoc
        val unLoc: Location
            get() = WDWorld.unLoc

        fun Player.currentWDGame(): WDGame? = GameManager.run { currentGame() }
    }
}
