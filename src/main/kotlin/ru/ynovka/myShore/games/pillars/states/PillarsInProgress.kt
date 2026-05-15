package ru.ynovka.myShore.games.pillars.states

import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.SpectatorReason
import ru.ynovka.myShore.games.pillars.PillarsGame
import ru.ynovka.myShore.games.pillars.PillarsPlayer
import ru.ynovka.myShore.games.pillars.PillarsWorld

class PillarsInProgress(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {


    override fun canPlayerJoin(gamePlayer: PillarsPlayer) = false
    override fun canPlayerBecomeSpectator(gamePlayer: PillarsPlayer, reason: SpectatorReason) = true
}