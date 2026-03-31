package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.worldDomination.WDPlayer


/**
 * Этап переговоров
 * Длится ровно 10 минут
 * В это время каждая страна может принять 1 другую страну на переговоры (5 минут)
 * И может отправить запрес на переговоры 1 другой страной (5 минут) (если страна откланила - не считается)
 */
object WDNegotiations : GameState<WDPlayer> {
    override fun onEnter(game: Game<WDPlayer>) { }

    override fun onExit(game: Game<WDPlayer>) { }

    override fun onPlayerJoin(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerReconnect(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun onPlayerLeave(game: Game<WDPlayer>, player: WDPlayer) { }

    override fun canPlayerJoin(game: Game<WDPlayer>, player: WDPlayer) = false
}