package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.games.GameState
import org.bukkit.entity.Player


/**
 * Этап переговоров
 * Длится ровно 10 минут
 * В это время каждая страна может принять 1 другую страну на переговоры (5 минут)
 * И может отправить запрес на переговоры 1 другой страной (5 минут) (если страна откланила - не считается)
 */
object WDNegotiationsState : GameState<WDGame> {
    override fun onStateStart(game: WDGame) {}

    override fun onPlayerJoin(game: WDGame, player: Player) {}
}