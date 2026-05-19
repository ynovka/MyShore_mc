package ru.ynovka.myShore.game.worldDomination

import ru.ynovka.myShore.game.worldDomination.states.WDWaitingForPlayers
import ru.ynovka.myShore.game.worldDomination.entity.WDGameHistory
import ru.ynovka.myShore.game.worldDomination.entity.Country
import ru.ynovka.myShore.game.GameManager
import ru.ynovka.myShore.game.GameWorld
import ru.ynovka.myShore.game.Game
import org.bukkit.entity.Player
import org.bukkit.Location
import java.util.UUID


class WDGame : Game<WDPlayer, GameWorld>() {
    override val initialState = WDWaitingForPlayers(this)
    override val maxPlayers = 50
    override val gamePlayers: MutableSet<WDPlayer> = mutableSetOf()
    override val gameWorld = WDWorld

    /** Текущий раунд игры */
    var round = 0
    /** Список стран */
    val countries: MutableSet<Country> = mutableSetOf()
    /** Мировой уровень экологии */
    var ecology: Double = ECOLOGY_START
        set(value) { field = value.coerceIn(0.0, 1.0) }
    /** История действий всех стран за игру */
    val history = WDGameHistory()


    override fun getOrCreatePlayer(playerId: UUID): WDPlayer =
        gamePlayers.firstOrNull { it.playerId == playerId}
            ?: WDPlayer(playerId)

    companion object {
        const val MIN_PLAYERS = 1 // todo заменить на 12
        const val ECOLOGY_START = 0.80

        val hubLoc: Location
            get() = WDWorld.hubLoc
        val unLoc: Location
            get() = WDWorld.unLoc

        fun UUID.currentWDGame(): WDGame? = GameManager.run { currentGame() }
    }
}
