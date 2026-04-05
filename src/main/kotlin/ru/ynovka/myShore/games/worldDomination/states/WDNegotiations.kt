package ru.ynovka.myShore.games.worldDomination.states

import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GamePlayer
import ru.ynovka.myShore.games.worldDomination.WDItems
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.worldDomination.WDPlayerRole


/**
 * Этап переговоров
 * Длится ровно 10 минут
 * В это время каждая страна может принять 1 другую страну на переговоры (5 минут)
 * И может отправить запрес на переговоры 1 другой страной (5 минут) (если страна откланила - не считается)
 */
class WDNegotiations(game: Game<WDPlayer>) : GameState<WDPlayer>(game) {
    override fun onEnter() {
        game.gamePlayers
            .filter { it.role == WDPlayerRole.PRESIDENT }
            .map(GamePlayer::player)
            .forEach { it.inventory.setItem(7, WDItems.wdPhoneMenu.getStack(null)) }

    }

    override fun onExit() {
        game.gamePlayers
            .filter { it.role == WDPlayerRole.PRESIDENT }
            .map(GamePlayer::player)
            .forEach { it.inventory.clear(7) }
    }

    override fun onPlayerJoin(player: WDPlayer) { }

    override fun onPlayerReconnect(player: WDPlayer) { }

    override fun onPlayerLeave(player: WDPlayer) { }

    override fun canPlayerJoin(player: WDPlayer): Boolean = false
}