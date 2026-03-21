package ru.ynovka.myShore.games.worldDomination.states

import org.bukkit.entity.Player
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.worldDomination.WDGame


// Ожидание игроков (нужно хотя бы 2)
object WDWaitingForPlayersState : GameState<WDGame> {
    override fun onStateStart(game: WDGame) {}

    override fun onPlayerJoin(game: WDGame, player: Player) {}
}